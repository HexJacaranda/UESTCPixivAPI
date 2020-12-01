package core.interfaces

data class FetchContext(val requires:Int,
                   val current:Int,
                   val expectedLimit:Int)