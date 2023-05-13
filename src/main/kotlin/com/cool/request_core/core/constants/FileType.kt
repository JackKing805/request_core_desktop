package com.cool.request_core.core.constants

/**
 * @className: FileType
 * @author: Jack
 * @date: 1/9/23
 **/
enum class FileType(val content:String) {
    FILE("file://");//example: file://path

    companion object{
        fun matchFileType(str:String): ResultFileType?{
            return if (str.startsWith(FILE.content)){
                return ResultFileType(FILE, getFilePath(FILE,str))
            }else{
                null
            }
        }

        private fun getFilePath(fileType: FileType, str:String):String{
            return str.substring(fileType.content.length)
        }
    }

    data class ResultFileType(
        val fileType: FileType,
        val fileName:String
    )
}

