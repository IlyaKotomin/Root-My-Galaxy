package dev.busung.s25uroot

import android.content.Context
import android.system.Os
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import org.json.JSONObject

data class VerifiedPayloads(
    val profile: TargetProfile,
    val exploit: File,
    val kernelSu: File,
)

/** A deliberately pinned, offline-only kit. Updating the kit requires a new APK. */
class PayloadRepository(private val context: Context) {
    private fun manifest(): ByteArray = context.assets.open("offline/targets.json").use { it.readBytes() }

    fun loadTargets(): List<TargetProfile> = SupportManifest.parse(manifest()).targets

    fun resolveTarget(snapshot: DeviceSnapshot): TargetProfile = loadTargets()
        .firstOrNull { it.matches(snapshot) }
        ?: error("The offline kit supports SM-S931B with kernel 6.6.98 only. No payload was run.")

    fun resolveTarget(profileId: String): TargetProfile = loadTargets()
        .firstOrNull { it.profileId == profileId }
        ?: error("This profile is not included in the offline kit.")

    fun download(profile: TargetProfile, onProgress: (String) -> Unit): VerifiedPayloads {
        // Recheck here as well: manual profile selection must never bypass compatibility.
        require(profile.matches(DeviceSnapshot.current())) { "The offline kit does not match this phone." }
        val trusted = resolveTarget(profile.profileId)
        val spec = JSONObject(manifest().toString(Charsets.UTF_8))
            .getJSONArray("payloads").getJSONObject(0)
        val directory = File(context.filesDir, "offline-kit").apply { mkdirs() }
        fun prepare(key: String, name: String): File {
            val metadata = spec.getJSONObject(key)
            val target = File(directory, name)
            val expectedHash = metadata.getString("sha256")
            val expectedSize = metadata.getLong("size")
            if (!verified(target, expectedSize, expectedHash)) {
                val temporary = File(directory, "$name.part")
                try {
                    context.assets.open("offline/${metadata.getString("url")}").use { input ->
                        FileOutputStream(temporary).use { output ->
                            input.copyTo(output)
                            output.fd.sync()
                        }
                    }
                    require(verified(temporary, expectedSize, expectedHash)) { "Offline $key failed SHA-256 verification." }
                    Os.chmod(temporary.absolutePath, 0b100100100)
                    Os.rename(temporary.absolutePath, target.absolutePath)
                } finally {
                    temporary.delete()
                }
            }
            onProgress("Offline $key verified (SHA-256)")
            return target
        }
        return VerifiedPayloads(trusted, prepare("exploit", "payload.so"), prepare("kernelsu", "ksud"))
    }

    private fun verified(file: File, size: Long, hash: String): Boolean = runCatching {
        if (!file.isFile || file.length() != size) return false
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) } == hash
    }.getOrDefault(false)
}
