package my.storyapp

import my.storyapp.data.retrofit.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val quote = ListStoryItem(
                i.toString(),
                "name + $i",
                "description $i",
                "photoUrl + $i",
                "createdAt + $i",
                i.toDouble(),
                i.toDouble()
            )
            items.add(quote)
        }
        return items
    }
}
