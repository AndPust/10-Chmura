package com.example.models
import kotlinx.serialization.Serializable

@Serializable
data class item (
    var id: Int = 0,
    var name: String = "Element",
    var description: String = "Default Element",
    var price: Int = 10,
    var img: String = "/resources/image.jpg",
    var qty: Int = 0
        )

val itemList = listOf(
    item(1, "Copper", "Limited edition periodic copper", 12, "/resources/copper.jpg", 1),
    item(2, "Gold", "Tech-grade gold!", 67, "/resources/gold.jpg", 1),
    item(3, "Lithium", "Keep it submerged and you'll be fine", 20, "/resources/lithium.jpg", 1),
    item(4, "Magnesium", "Say goodbye to cramps!", 16, "/resources/magnesium.jpg", 1),
    item(5, "Titanium", "For bones, bicycles, rockets!", 55, "/resources/titanium.jpg", 1),
    item(6, "Mercury", "Careful not to spill it.", 21, "/resources/mercury.jpg", 1),
    item(7, "Sulfur", "For all your stinky needs!", 11, "/resources/sulfur.jpg", 1)
)