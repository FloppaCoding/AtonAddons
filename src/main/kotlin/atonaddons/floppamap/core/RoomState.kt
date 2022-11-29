package atonaddons.floppamap.core

enum class RoomState {
    CLEARED,
    DISCOVERED,
    FAILED,
    GREEN,
    UNDISCOVERED,
    UNKNOWN; // Used for the question mark rooms;

    val revealed: Boolean
        get() = this != UNDISCOVERED && this != UNKNOWN

}
