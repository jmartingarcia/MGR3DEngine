# MGR3DEngine

A personal **Java / OpenGL** project: a small 3D engine (LWJGL) with a procedural-map demo. Built to stay hands-on with **systems fundamentals**—fixed-timestep loops, GPU shaders, spatial data structures, and real-time performance—not as a commercial game product.

Demo: https://www.youtube.com/watch?v=ePsJM23K5L4

---

## Highlights

| Area | Implementation |
|------|----------------|
| **Game loop** | Fixed update rate (30 UPS) + variable render rate (75 FPS target), accumulator pattern |
| **Rendering** | OpenGL 3.3, custom GLSL shaders (Phong-style lighting: directional, point, spot) |
| **Geometry** | VAO/VBO meshes, textures, materials, view/projection math (JOML) |
| **World** | Procedural multi-room maps, doors, themed materials, dynamic lighting |
| **Collision** | Axis-aligned bounding boxes (AABB), room-aware wall checks |
| **Architecture** | Engine layer (`com.mgr.engine`) separated from demo game (`com.mgr.myshooter`) via `IGameLogic` |
| **Tooling** | Gradle, LWJGL 3, property-driven asset paths |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Main → GameEngine (loop, input, timer, window)         │
│           │                                             │
│           ▼                                             │
│  IGameLogic ← MGRGame (demo: movement, world, UI)       │
│           │                                             │
│           ├── Renderer → World → RandomMap / Rooms      │
│           │              └── Meshes, lights, items      │
│           └── Player → Camera, crosshair, collision     │
│                                                         │
│  com.mgr.engine: ShaderProgram, Mesh, Texture,          │
│                  lights, Timer, MouseInput, Profiler    │
└─────────────────────────────────────────────────────────┘
```

**Design intent:** The engine owns the loop and rendering primitives; the game implements `IGameLogic` and can be swapped without rewriting OpenGL boilerplate.

---

## Tech stack

- **Java** (JDK 8+)
- **[LWJGL 3](https://www.lwjgl.org/)** — GLFW window, OpenGL
- **[JOML](https://github.com/JOML-CI/JOML)** — matrices and vectors
- **Gradle** — build and native dependencies per OS
- **GLSL** — vertex/fragment shaders under `src/main/resources/Shaders/`

---

## Prerequisites

- **JDK 8 or newer**
- **Gradle** (wrapper included: `./gradlew`)
- A machine with **OpenGL 3.3+** support
- **macOS**, **Windows**, or **Linux** (LWJGL natives are selected automatically in `build.gradle`)

---

## Configuration

Asset paths are loaded from `src/main/resources/config.properties`. Before running, point them at your local clone:

```properties
base.folder.textures=/path/to/MGR3DEngine/src/main/resources/Textures
base.folder.shaders=/path/to/MGR3DEngine/src/main/resources/Shaders
```

Use forward slashes on macOS/Linux. The default file may still contain Windows paths from the original dev machine.

---

## Build and run

```bash
git clone https://github.com/jmartingarcia/MGR3DEngine.git
cd MGR3DEngine
# Edit config.properties (see above)
./gradlew build
```

**Run the demo**

- **IDE (recommended):** Run main class `com.mgr.myshooter.Main`
- **Command line:** After `./gradlew build`, run with your IDE’s classpath or add the Gradle `application` plugin if you prefer `./gradlew run`

The demo opens a window titled **MGR GAME** (default 3000×2000; vSync on).

---

## Controls

| Key | Action |
|-----|--------|
| **W / A / S / D** | Move |
| **Space / V** | Move up / down |
| **Left / Right arrow** | Rotate (keyboard) |
| **Right mouse (hold)** | Look around |
| **T** | Toggle on-screen profiler (FPS, camera, room count) |
| **I** | Generate a new procedural map |
| **E** | Interact with nearest world item |
| **N / M** | Advance / rewind in-world time (lighting demo) |

---

## Project layout

```
src/main/java/com/mgr/engine/          # Reusable engine: loop, GL, shaders, mesh, lights
src/main/java/com/mgr/myshooter/        # Demo game: world, map gen, player, renderer
src/main/java/com/mgr/configuration/    # PropertiesLoader
src/main/resources/Shaders/             # GLSL (lit + simple text overlay)
src/main/resources/Textures/            # Theme textures (e.g. spaceship)
src/main/resources/config.properties      # Local path configuration
```

## License

Personal project. All rights reserved unless a license file is added later.
