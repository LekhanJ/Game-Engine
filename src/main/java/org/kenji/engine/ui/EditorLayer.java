package org.kenji.engine.ui;

import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.kenji.engine.Window;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class EditorLayer {

    private final long window;

    private final ImGuiImplGlfw imGuiGlfw;
    private final ImGuiImplGl3 imGuiGl3;

    public EditorLayer(long window) {
        this.window = window;
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();
    }

    public void init() {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();

        ImFontAtlas atlas = io.getFonts();

        atlas.addFontFromFileTTF(
                "assets/fonts/CaskaydiaCoveNerdFont-Regular.ttf",
                18
        );
        atlas.addFontFromFileTTF(
                "assets/fonts/CaskaydiaCoveNerdFont-Bold.ttf",
                24
        );

        // Don't create imgui.ini
        io.setIniFilename(null);

        // Enable keyboard navigation
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        // Enable docking
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        // Enable multiple OS windows (optional)
        // io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);

        /*
         * false means ImGui WILL NOT install its own GLFW callbacks.
         * the engine continues to own all input callbacks.
         */
        imGuiGlfw.init(window, false);

        // Match your OpenGL version.
        imGuiGl3.init("#version 330 core");
    }

    public void beginFrame(float dt) {
        ImGuiIO io = ImGui.getIO();

        io.setDisplaySize(Window.getWidth(), Window.getHeight());
        io.setDisplayFramebufferScale(1.0f, 1.0f);
        io.setDeltaTime(dt);

        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();

        ImGui.newFrame();
    }

    public void render() {
        ImGui.begin("Inspector");

        if (ImGui.button("Click Me")) {
            System.out.println("Clicked!");
        }

        ImGui.end();
    }

    public void endFrame() {
        ImGui.render();

        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            long backup = glfwGetCurrentContext();

            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();

            glfwMakeContextCurrent(backup);
        }
    }

    public void dispose() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }

    public ImGuiImplGlfw getGlfw() {
        return imGuiGlfw;
    }
}