package com.example.scentguard.data.model

import com.example.scentguard.R

data class MascotAvatar(
    val id: String,
    val name: String,
    val resId: Int,
    val personality: String
)

object MascotAvatars {
    val collection = listOf(
        MascotAvatar("penguin", "Penguin", R.drawable.ic_mascot_penguin, "Calm, clean, reliable"),
        MascotAvatar("fox", "Fox", R.drawable.ic_mascot_fox, "Intelligent, confident"),
        MascotAvatar("panda", "Panda", R.drawable.ic_mascot_panda, "Friendly, approachable"),
        MascotAvatar("cat", "Cat", R.drawable.ic_mascot_cat, "Calm, precise, observant"),
        MascotAvatar("wolf", "Wolf", R.drawable.ic_mascot_wolf, "Strong, dependable, protective"),
        MascotAvatar("owl", "Owl", R.drawable.ic_mascot_owl, "Intelligent, analytical, watchful"),
        MascotAvatar("robot", "Robot", R.drawable.ic_mascot_robot, "Futuristic, technological, efficient"),
        MascotAvatar("bear", "Bear", R.drawable.ic_mascot_bear, "Reliable, protective, strong")
    )

    fun getById(id: String?): MascotAvatar? {
        return collection.find { it.id == id }
    }
}
