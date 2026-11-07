package com.oritong.superflatcore.blackhole.client;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

/** Camera/depth math regressions; runs without Minecraft or an OpenGL context. */
public final class BlackHoleMatricesTest {
    public static void main(String[] args) {
        Matrix4f view = new Matrix4f().rotateX(.3f).rotateY(1.2f).rotateZ(.12f);
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70), 16f / 9f, .05f, 1024f)
                .translate(.02f, -.03f, .01f).rotateZ(.08f);
        BlackHoleMatrices.Frame baseline = requireFrame(view, projection);
        assertRoundTrip(baseline);

        // Matrices from another renderer can have correct values and stale
        // JOML shortcut flags. The captured transform must still be identical.
        Matrix4f staleView = new Matrix4f(view).assume(Matrix4fc.PROPERTY_IDENTITY);
        Matrix4f staleProjection = new Matrix4f(projection).assume(Matrix4fc.PROPERTY_AFFINE);
        BlackHoleMatrices.Frame corrected = requireFrame(staleView, staleProjection);
        if (!baseline.viewProjection().equals(corrected.viewProjection(), .00001f)) {
            throw new AssertionError("Stale matrix flags changed world projection");
        }
        assertRoundTrip(corrected);

        // Later stages mutate the shared event matrices; this frame's depth
        // reconstruction must continue to use its independent camera snapshot.
        view.zero();
        projection.zero();
        assertRoundTrip(baseline);
        assertRejected(new Matrix4f(), projection, "zero projection");
        assertRejected(new Matrix4f().scale(1, 0, 1), newProjection(), "singular view");
        assertRejected(new Matrix4f().m00(Float.NaN), newProjection(), "non-finite view");
        assertRejected(new Matrix4f(), new Matrix4f().m00(Float.POSITIVE_INFINITY), "non-finite projection");
        assertRejected(new Matrix4f(), new Matrix4f().ortho(-1, 1, -1, 1, .05f, 100), "non-world camera");

        // A subsequent valid frame is usable immediately after bad inputs.
        assertRoundTrip(requireFrame(new Matrix4f(), newProjection()));
        System.out.println("Passed black-hole camera snapshot, depth round-trip, stale flag and invalid frame checks.");
    }

    private static Matrix4f newProjection() {
        return new Matrix4f().perspective((float) Math.toRadians(70), 16f / 9f, .05f, 1024f);
    }

    private static BlackHoleMatrices.Frame requireFrame(Matrix4f view, Matrix4f projection) {
        BlackHoleMatrices.Frame frame = BlackHoleMatrices.capture(view, projection);
        if (frame == null) {
            throw new AssertionError("Valid world camera was rejected");
        }
        return frame;
    }

    private static void assertRejected(Matrix4f view, Matrix4f projection, String label) {
        if (BlackHoleMatrices.capture(view, projection) != null) {
            throw new AssertionError("Accepted " + label);
        }
    }

    private static void assertRoundTrip(BlackHoleMatrices.Frame frame) {
        for (Vector4f point : new Vector4f[]{new Vector4f(.5f, -.25f, -8, 1), new Vector4f(-2, 3, -40, 1)}) {
            Vector4f projected = new Vector4f(point).mul(frame.viewProjection());
            projected.div(projected.w);
            Vector4f restored = projected.mul(frame.inverseViewProjection());
            restored.div(restored.w);
            if (!point.equals(restored, .01f)) {
                throw new AssertionError("Depth reconstruction moved a world point: " + point + " -> " + restored);
            }
        }
    }
}
