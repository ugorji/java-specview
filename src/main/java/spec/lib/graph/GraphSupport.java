package spec.lib.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/*
 ******************************************
 * Class to support the graph Package ...
 * contains some hash map initializers
 ******************************************
 */
public final class GraphSupport {

  /** A map, mapping percentage value to a color */
  public static final Map blackBackgroundColorMap = new HashMap(120, 0.97f);

  public static final Map whiteBackgroundColorMap = new HashMap(120, 0.97f);

  // initializer ... initializing the color table
  static {
    blackBackgroundColorMap.put(new Byte((byte) 0), new Color(0f, 0f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 1), new Color(0f, 0f, 0.25f));
    blackBackgroundColorMap.put(new Byte((byte) 2), new Color(0f, 0f, 0.5f));
    blackBackgroundColorMap.put(new Byte((byte) 3), new Color(0f, 0f, 0.5f));
    blackBackgroundColorMap.put(new Byte((byte) 4), new Color(0f, 0f, 0.56f));
    blackBackgroundColorMap.put(new Byte((byte) 5), new Color(0f, 0.01f, 0.61f));
    blackBackgroundColorMap.put(new Byte((byte) 6), new Color(0f, 0.01f, 0.61f));
    blackBackgroundColorMap.put(new Byte((byte) 7), new Color(0f, 0.01f, 0.67f));
    blackBackgroundColorMap.put(new Byte((byte) 8), new Color(0f, 0.02f, 0.72f));
    blackBackgroundColorMap.put(new Byte((byte) 9), new Color(0f, 0.02f, 0.72f));
    blackBackgroundColorMap.put(new Byte((byte) 10), new Color(0f, 0.03f, 0.76f));
    blackBackgroundColorMap.put(new Byte((byte) 11), new Color(0f, 0.04f, 0.8f));
    blackBackgroundColorMap.put(new Byte((byte) 12), new Color(0f, 0.04f, 0.8f));
    blackBackgroundColorMap.put(new Byte((byte) 13), new Color(0f, 0.05f, 0.84f));
    blackBackgroundColorMap.put(new Byte((byte) 14), new Color(0f, 0.05f, 0.84f));
    blackBackgroundColorMap.put(new Byte((byte) 15), new Color(0f, 0.06f, 0.87f));
    blackBackgroundColorMap.put(new Byte((byte) 16), new Color(0f, 0.07f, 0.9f));
    blackBackgroundColorMap.put(new Byte((byte) 17), new Color(0f, 0.07f, 0.9f));
    blackBackgroundColorMap.put(new Byte((byte) 18), new Color(0f, 0.08f, 0.93f));
    blackBackgroundColorMap.put(new Byte((byte) 19), new Color(0f, 0.09f, 0.95f));
    blackBackgroundColorMap.put(new Byte((byte) 20), new Color(0f, 0.09f, 0.95f));
    blackBackgroundColorMap.put(new Byte((byte) 21), new Color(0f, 0.1f, 0.97f));
    blackBackgroundColorMap.put(new Byte((byte) 22), new Color(0f, 0.12f, 0.98f));
    blackBackgroundColorMap.put(new Byte((byte) 23), new Color(0f, 0.12f, 0.98f));
    blackBackgroundColorMap.put(new Byte((byte) 24), new Color(0f, 0.13f, 0.99f));
    blackBackgroundColorMap.put(new Byte((byte) 25), new Color(0f, 0.13f, 0.99f));
    blackBackgroundColorMap.put(new Byte((byte) 26), new Color(0f, 0.14f, 0.99f));
    blackBackgroundColorMap.put(new Byte((byte) 27), new Color(0f, 0.16f, 0.99f));
    blackBackgroundColorMap.put(new Byte((byte) 28), new Color(0f, 0.16f, 0.99f));
    blackBackgroundColorMap.put(new Byte((byte) 29), new Color(0f, 0.18f, 0.99f));
    blackBackgroundColorMap.put(new Byte((byte) 30), new Color(0f, 0.19f, 0.98f));
    blackBackgroundColorMap.put(new Byte((byte) 31), new Color(0f, 0.19f, 0.98f));
    blackBackgroundColorMap.put(new Byte((byte) 32), new Color(0f, 0.21f, 0.97f));
    blackBackgroundColorMap.put(new Byte((byte) 33), new Color(0f, 0.23f, 0.96f));
    blackBackgroundColorMap.put(new Byte((byte) 34), new Color(0f, 0.23f, 0.96f));
    blackBackgroundColorMap.put(new Byte((byte) 35), new Color(0f, 0.25f, 0.94f));
    blackBackgroundColorMap.put(new Byte((byte) 36), new Color(0f, 0.27f, 0.92f));
    blackBackgroundColorMap.put(new Byte((byte) 37), new Color(0f, 0.27f, 0.92f));
    blackBackgroundColorMap.put(new Byte((byte) 38), new Color(0f, 0.29f, 0.89f));
    blackBackgroundColorMap.put(new Byte((byte) 39), new Color(0f, 0.29f, 0.89f));
    blackBackgroundColorMap.put(new Byte((byte) 40), new Color(0f, 0.31f, 0.86f));
    blackBackgroundColorMap.put(new Byte((byte) 41), new Color(0f, 0.33f, 0.82f));
    blackBackgroundColorMap.put(new Byte((byte) 42), new Color(0f, 0.33f, 0.82f));
    blackBackgroundColorMap.put(new Byte((byte) 43), new Color(0f, 0.35f, 0.78f));
    blackBackgroundColorMap.put(new Byte((byte) 44), new Color(0f, 0.37f, 0.74f));
    blackBackgroundColorMap.put(new Byte((byte) 45), new Color(0f, 0.37f, 0.74f));
    blackBackgroundColorMap.put(new Byte((byte) 46), new Color(0f, 0.4f, 0.69f));
    blackBackgroundColorMap.put(new Byte((byte) 47), new Color(0f, 0.42f, 0.64f));
    blackBackgroundColorMap.put(new Byte((byte) 48), new Color(0f, 0.42f, 0.64f));
    blackBackgroundColorMap.put(new Byte((byte) 49), new Color(0f, 0.45f, 0.59f));
    blackBackgroundColorMap.put(new Byte((byte) 50), new Color(0f, 0.45f, 0.59f));
    blackBackgroundColorMap.put(new Byte((byte) 51), new Color(0f, 0.47f, 0.53f));
    blackBackgroundColorMap.put(new Byte((byte) 52), new Color(0f, 0.5f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 53), new Color(0f, 0.5f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 54), new Color(0.01f, 0.53f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 55), new Color(0.02f, 0.56f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 56), new Color(0.02f, 0.56f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 57), new Color(0.04f, 0.59f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 58), new Color(0.06f, 0.61f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 59), new Color(0.06f, 0.61f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 60), new Color(0.09f, 0.65f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 61), new Color(0.13f, 0.68f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 62), new Color(0.13f, 0.68f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 63), new Color(0.17f, 0.71f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 64), new Color(0.17f, 0.71f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 65), new Color(0.23f, 0.74f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 66), new Color(0.29f, 0.77f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 67), new Color(0.29f, 0.77f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 68), new Color(0.37f, 0.81f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 69), new Color(0.46f, 0.84f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 70), new Color(0.46f, 0.84f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 71), new Color(0.56f, 0.88f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 72), new Color(0.68f, 0.91f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 73), new Color(0.68f, 0.91f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 74), new Color(0.81f, 0.95f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 75), new Color(0.81f, 0.95f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 76), new Color(0.96f, 0.99f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 77), new Color(1f, 1f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 78), new Color(1f, 1f, 0f));
    blackBackgroundColorMap.put(new Byte((byte) 79), new Color(1f, 1f, 0.01f));
    blackBackgroundColorMap.put(new Byte((byte) 80), new Color(1f, 1f, 0.02f));
    blackBackgroundColorMap.put(new Byte((byte) 81), new Color(1f, 1f, 0.02f));
    blackBackgroundColorMap.put(new Byte((byte) 82), new Color(1f, 1f, 0.04f));
    blackBackgroundColorMap.put(new Byte((byte) 83), new Color(1f, 1f, 0.07f));
    blackBackgroundColorMap.put(new Byte((byte) 84), new Color(1f, 1f, 0.07f));
    blackBackgroundColorMap.put(new Byte((byte) 85), new Color(1f, 1f, 0.1f));
    blackBackgroundColorMap.put(new Byte((byte) 86), new Color(1f, 1f, 0.14f));
    blackBackgroundColorMap.put(new Byte((byte) 87), new Color(1f, 1f, 0.14f));
    blackBackgroundColorMap.put(new Byte((byte) 88), new Color(1f, 1f, 0.19f));
    blackBackgroundColorMap.put(new Byte((byte) 89), new Color(1f, 1f, 0.19f));
    blackBackgroundColorMap.put(new Byte((byte) 90), new Color(1f, 1f, 0.24f));
    blackBackgroundColorMap.put(new Byte((byte) 91), new Color(1f, 1f, 0.31f));
    blackBackgroundColorMap.put(new Byte((byte) 92), new Color(1f, 1f, 0.31f));
    blackBackgroundColorMap.put(new Byte((byte) 93), new Color(1f, 1f, 0.39f));
    blackBackgroundColorMap.put(new Byte((byte) 94), new Color(1f, 1f, 0.48f));
    blackBackgroundColorMap.put(new Byte((byte) 95), new Color(1f, 1f, 0.48f));
    blackBackgroundColorMap.put(new Byte((byte) 96), new Color(1f, 1f, 0.59f));
    blackBackgroundColorMap.put(new Byte((byte) 97), new Color(1f, 1f, 0.71f));
    blackBackgroundColorMap.put(new Byte((byte) 98), new Color(1f, 1f, 0.71f));
    blackBackgroundColorMap.put(new Byte((byte) 99), new Color(1f, 1f, 0.84f));
    blackBackgroundColorMap.put(new Byte((byte) 100), new Color(1f, 1f, 0.84f));
  }

  static {
    whiteBackgroundColorMap.put(new Byte((byte) 0), new Color(1f, 1f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 1), new Color(1f, 1f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 2), new Color(1f, 1f, 0.5f));
    whiteBackgroundColorMap.put(new Byte((byte) 3), new Color(1f, 1f, 0.5f));
    whiteBackgroundColorMap.put(new Byte((byte) 4), new Color(1f, 1f, 0.44f));
    whiteBackgroundColorMap.put(new Byte((byte) 5), new Color(1f, 0.99f, 0.39f));
    whiteBackgroundColorMap.put(new Byte((byte) 6), new Color(1f, 0.99f, 0.39f));
    whiteBackgroundColorMap.put(new Byte((byte) 7), new Color(1f, 0.99f, 0.33f));
    whiteBackgroundColorMap.put(new Byte((byte) 8), new Color(1f, 0.98f, 0.28f));
    whiteBackgroundColorMap.put(new Byte((byte) 9), new Color(1f, 0.98f, 0.28f));
    whiteBackgroundColorMap.put(new Byte((byte) 10), new Color(1f, 0.97f, 0.24f));
    whiteBackgroundColorMap.put(new Byte((byte) 11), new Color(1f, 0.96f, 0.2f));
    whiteBackgroundColorMap.put(new Byte((byte) 12), new Color(1f, 0.96f, 0.2f));
    whiteBackgroundColorMap.put(new Byte((byte) 13), new Color(1f, 0.95f, 0.16f));
    whiteBackgroundColorMap.put(new Byte((byte) 14), new Color(1f, 0.95f, 0.16f));
    whiteBackgroundColorMap.put(new Byte((byte) 15), new Color(1f, 0.94f, 0.13f));
    whiteBackgroundColorMap.put(new Byte((byte) 16), new Color(1f, 0.93f, 0.1f));
    whiteBackgroundColorMap.put(new Byte((byte) 17), new Color(1f, 0.93f, 0.1f));
    whiteBackgroundColorMap.put(new Byte((byte) 18), new Color(1f, 0.92f, 0.07f));
    whiteBackgroundColorMap.put(new Byte((byte) 19), new Color(1f, 0.91f, 0.05f));
    whiteBackgroundColorMap.put(new Byte((byte) 20), new Color(1f, 0.91f, 0.05f));
    whiteBackgroundColorMap.put(new Byte((byte) 21), new Color(1f, 0.9f, 0.03f));
    whiteBackgroundColorMap.put(new Byte((byte) 22), new Color(1f, 0.88f, 0.02f));
    whiteBackgroundColorMap.put(new Byte((byte) 23), new Color(1f, 0.88f, 0.02f));
    whiteBackgroundColorMap.put(new Byte((byte) 24), new Color(1f, 0.87f, 0.01f));
    whiteBackgroundColorMap.put(new Byte((byte) 25), new Color(1f, 0.87f, 0.01f));
    whiteBackgroundColorMap.put(new Byte((byte) 26), new Color(1f, 0.86f, 0.01f));
    whiteBackgroundColorMap.put(new Byte((byte) 27), new Color(1f, 0.84f, 0.01f));
    whiteBackgroundColorMap.put(new Byte((byte) 28), new Color(1f, 0.84f, 0.01f));
    whiteBackgroundColorMap.put(new Byte((byte) 29), new Color(1f, 0.82f, 0.01f));
    whiteBackgroundColorMap.put(new Byte((byte) 30), new Color(1f, 0.81f, 0.02f));
    whiteBackgroundColorMap.put(new Byte((byte) 31), new Color(1f, 0.81f, 0.02f));
    whiteBackgroundColorMap.put(new Byte((byte) 32), new Color(1f, 0.79f, 0.03f));
    whiteBackgroundColorMap.put(new Byte((byte) 33), new Color(1f, 0.77f, 0.04f));
    whiteBackgroundColorMap.put(new Byte((byte) 34), new Color(1f, 0.77f, 0.04f));
    whiteBackgroundColorMap.put(new Byte((byte) 35), new Color(1f, 0.75f, 0.06f));
    whiteBackgroundColorMap.put(new Byte((byte) 36), new Color(1f, 0.73f, 0.08f));
    whiteBackgroundColorMap.put(new Byte((byte) 37), new Color(1f, 0.73f, 0.08f));
    whiteBackgroundColorMap.put(new Byte((byte) 38), new Color(1f, 0.71f, 0.11f));
    whiteBackgroundColorMap.put(new Byte((byte) 39), new Color(1f, 0.71f, 0.11f));
    whiteBackgroundColorMap.put(new Byte((byte) 40), new Color(1f, 0.69f, 0.14f));
    whiteBackgroundColorMap.put(new Byte((byte) 41), new Color(1f, 0.67f, 0.18f));
    whiteBackgroundColorMap.put(new Byte((byte) 42), new Color(1f, 0.67f, 0.18f));
    whiteBackgroundColorMap.put(new Byte((byte) 43), new Color(1f, 0.65f, 0.22f));
    whiteBackgroundColorMap.put(new Byte((byte) 44), new Color(1f, 0.63f, 0.26f));
    whiteBackgroundColorMap.put(new Byte((byte) 45), new Color(1f, 0.63f, 0.26f));
    whiteBackgroundColorMap.put(new Byte((byte) 46), new Color(1f, 0.6f, 0.31f));
    whiteBackgroundColorMap.put(new Byte((byte) 47), new Color(1f, 0.58f, 0.36f));
    whiteBackgroundColorMap.put(new Byte((byte) 48), new Color(1f, 0.58f, 0.36f));
    whiteBackgroundColorMap.put(new Byte((byte) 49), new Color(1f, 0.55f, 0.41f));
    whiteBackgroundColorMap.put(new Byte((byte) 50), new Color(1f, 0.55f, 0.41f));
    whiteBackgroundColorMap.put(new Byte((byte) 51), new Color(1f, 0.53f, 0.47f));
    whiteBackgroundColorMap.put(new Byte((byte) 52), new Color(1f, 0.5f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 53), new Color(1f, 0.5f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 54), new Color(0.99f, 0.47f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 55), new Color(0.98f, 0.44f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 56), new Color(0.98f, 0.44f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 57), new Color(0.96f, 0.41f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 58), new Color(0.94f, 0.39f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 59), new Color(0.94f, 0.39f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 60), new Color(0.91f, 0.35f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 61), new Color(0.87f, 0.32f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 62), new Color(0.87f, 0.32f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 63), new Color(0.83f, 0.29f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 64), new Color(0.83f, 0.29f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 65), new Color(0.77f, 0.26f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 66), new Color(0.71f, 0.23f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 67), new Color(0.71f, 0.23f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 68), new Color(0.63f, 0.19f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 69), new Color(0.54f, 0.16f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 70), new Color(0.54f, 0.16f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 71), new Color(0.44f, 0.12f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 72), new Color(0.32f, 0.09f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 73), new Color(0.32f, 0.09f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 74), new Color(0.19f, 0.05f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 75), new Color(0.19f, 0.05f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 76), new Color(0.04f, 0.01f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 77), new Color(0f, 0f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 78), new Color(0f, 0f, 1f));
    whiteBackgroundColorMap.put(new Byte((byte) 79), new Color(0f, 0f, 0.99f));
    whiteBackgroundColorMap.put(new Byte((byte) 80), new Color(0f, 0f, 0.98f));
    whiteBackgroundColorMap.put(new Byte((byte) 81), new Color(0f, 0f, 0.98f));
    whiteBackgroundColorMap.put(new Byte((byte) 82), new Color(0f, 0f, 0.96f));
    whiteBackgroundColorMap.put(new Byte((byte) 83), new Color(0f, 0f, 0.93f));
    whiteBackgroundColorMap.put(new Byte((byte) 84), new Color(0f, 0f, 0.93f));
    whiteBackgroundColorMap.put(new Byte((byte) 85), new Color(0f, 0f, 0.9f));
    whiteBackgroundColorMap.put(new Byte((byte) 86), new Color(0f, 0f, 0.86f));
    whiteBackgroundColorMap.put(new Byte((byte) 87), new Color(0f, 0f, 0.86f));
    whiteBackgroundColorMap.put(new Byte((byte) 88), new Color(0f, 0f, 0.81f));
    whiteBackgroundColorMap.put(new Byte((byte) 89), new Color(0f, 0f, 0.81f));
    whiteBackgroundColorMap.put(new Byte((byte) 90), new Color(0f, 0f, 0.76f));
    whiteBackgroundColorMap.put(new Byte((byte) 91), new Color(0f, 0f, 0.69f));
    whiteBackgroundColorMap.put(new Byte((byte) 92), new Color(0f, 0f, 0.69f));
    whiteBackgroundColorMap.put(new Byte((byte) 93), new Color(0f, 0f, 0.61f));
    whiteBackgroundColorMap.put(new Byte((byte) 94), new Color(0f, 0f, 0.52f));
    whiteBackgroundColorMap.put(new Byte((byte) 95), new Color(0f, 0f, 0.52f));
    whiteBackgroundColorMap.put(new Byte((byte) 96), new Color(0f, 0f, 0.41f));
    whiteBackgroundColorMap.put(new Byte((byte) 97), new Color(0f, 0f, 0.29f));
    whiteBackgroundColorMap.put(new Byte((byte) 98), new Color(0f, 0f, 0.29f));
    whiteBackgroundColorMap.put(new Byte((byte) 99), new Color(0f, 0f, 0.16f));
    whiteBackgroundColorMap.put(new Byte((byte) 100), new Color(0f, 0f, 0.16f));
  }

  /** no-one can instantiate this class */
  private GraphSupport() {}
}
