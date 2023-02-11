package com.example.musicbell

class Utility {
    companion object {
        fun GetRandomIndex(maxIndex: Int): Int{
            return (0..maxIndex).random()
        }
    }
}