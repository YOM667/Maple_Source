/*
 * Decompiled with CFR 0_132.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.util.glu.GLU
 */
package top.youm.maple.utils.render.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import top.youm.maple.utils.math.Vec3f;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public final class GLUtils {
    public static final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer((int)16);
    public static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer((int)16);
    public static final IntBuffer VIEWPORT = BufferUtils.createIntBuffer((int)16);
    public static final FloatBuffer TO_SCREEN_BUFFER = BufferUtils.createFloatBuffer((int)3);
    public static final FloatBuffer TO_WORLD_BUFFER = BufferUtils.createFloatBuffer((int)3);

    private GLUtils() {
    }

    public static void init() {
    }

    public static float[] getColor(int hex) {
        return new float[]{(float)(hex >> 16 & 255) / 255.0f, (float)(hex >> 8 & 255) / 255.0f, (float)(hex & 255) / 255.0f, (float)(hex >> 24 & 255) / 255.0f};
    }

    public static void glColor(int hex) {
        float[] color = GLUtils.getColor(hex);
        GlStateManager.color(color[0], color[1], color[2], color[3]);
    }

    public static void rotateX(float angle, double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(angle, 1.0f, 0.0f, 0.0f);
        GlStateManager.translate(- x, - y, - z);
    }

    public static void rotateY(float angle, double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(- x, - y, - z);
    }

    public static void rotateZ(float angle, double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(angle, 0.0f, 0.0f, 1.0f);
        GlStateManager.translate(- x, - y, - z);
    }

    public static Vec3f toScreen(Vec3f pos) {
        return GLUtils.toScreen(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3f toScreen(double x, double y, double z) {
        boolean result = GLU.gluProject((float)((float)x), (float)((float)y), (float)((float)z), (FloatBuffer)MODELVIEW, (FloatBuffer)PROJECTION, (IntBuffer)VIEWPORT, (FloatBuffer)((FloatBuffer)TO_SCREEN_BUFFER.clear()));
        if (result) {
            return new Vec3f(TO_SCREEN_BUFFER.get(0), (float)Display.getHeight() - TO_SCREEN_BUFFER.get(1), TO_SCREEN_BUFFER.get(2));
        }
        return null;
    }

    public static Vec3f toWorld(Vec3f pos) {
        return GLUtils.toWorld(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3f toWorld(double x, double y, double z) {
        boolean result = GLU.gluUnProject((float)((float)x), (float)((float)y), (float)((float)z), (FloatBuffer)MODELVIEW, (FloatBuffer)PROJECTION, (IntBuffer)VIEWPORT, (FloatBuffer)((FloatBuffer)TO_WORLD_BUFFER.clear()));
        if (result) {
            return new Vec3f(TO_WORLD_BUFFER.get(0), TO_WORLD_BUFFER.get(1), TO_WORLD_BUFFER.get(2));
        }
        return null;
    }

    public static FloatBuffer getModelview() {
        return MODELVIEW;
    }

    public static FloatBuffer getProjection() {
        return PROJECTION;
    }

    public static IntBuffer getViewport() {
        return VIEWPORT;
    }
    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    public static void endBlend() {
        GlStateManager.disableBlend();
    }
    public static void setup2DRendering(boolean blend) {
        if (blend) {
            startBlend();
        }
        GlStateManager.disableTexture2D();
    }

    public static void setup2DRendering() {
        setup2DRendering(true);
    }

    public static void end2DRendering() {
        GlStateManager.enableTexture2D();
        endBlend();
    }

}

