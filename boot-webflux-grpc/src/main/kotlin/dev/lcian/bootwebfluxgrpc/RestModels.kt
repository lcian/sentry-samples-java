package dev.lcian.bootwebfluxgrpc

data class RestUploadRequest(
    val deviceId: String = "",
    val fileName: String = ""
) {
    fun toProto(): UploadRequest {
        return UploadRequest.newBuilder()
            .setDeviceId(deviceId)
            .setFileName(fileName)
            .build()
    }
}

data class RestUploadUrlResponse(
    val uploadUrl: String
) {
    companion object {
        fun fromProto(proto: GetUploadUrlResponse): RestUploadUrlResponse {
            return RestUploadUrlResponse(
                uploadUrl = proto.uploadUrl
            )
        }
    }
} 