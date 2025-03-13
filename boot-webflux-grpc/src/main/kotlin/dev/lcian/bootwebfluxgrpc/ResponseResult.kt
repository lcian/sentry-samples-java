package dev.lcian.bootwebfluxgrpc

data class ResponseResult(
    val success: Boolean,
    val data: Any? = null,
    val message: String? = null
) {
    companion object {
        fun success(data: Any? = null): ResponseResult {
            return ResponseResult(true, data)
        }

        fun error(message: String): ResponseResult {
            return ResponseResult(false, message = message)
        }
    }
} 