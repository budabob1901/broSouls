package main;

import objects.Door;
import java.util.ArrayList;

public class ObjectManager {

    public ArrayList<Door> doors = new ArrayList<>();

    public void addDoor(Door d) {
        doors.add(d);
    }
}