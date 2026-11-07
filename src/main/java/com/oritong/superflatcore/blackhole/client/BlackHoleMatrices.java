package com.oritong.superflatcore.blackhole.client;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

/** Validated copies of a world camera; invalid frames never become persistent failures. */
final class BlackHoleMatrices {
    private BlackHoleMatrices() {}

    record Frame(Matrix4f view, Matrix4f viewProjection, Matrix4f inverseViewProjection) {}

    static Frame capture(Matrix4fc view, Matrix4fc projection) {
        if (!view.isFinite() || !projection.isFinite()) {
            return null;
        }
        // Recompute JOML's optimization flags from the values. Other renderers
        // can update shared matrices without keeping those flags in sync.
        Matrix4f viewCopy = new Matrix4f(view).determineProperties();
        Matrix4f projectionCopy = new Matrix4f(projection).determineProperties();
        Matrix4f vp = projectionCopy.mul(viewCopy);
        Matrix4f inverse = new Matrix4f(vp).invert();
        if (!vp.isFinite() || !inverse.isFinite()) {
            return null;
        }
        Vector4f eye = new Vector4f(0, 0, 1, 0).mul(inverse);
        if (!eye.isFinite() || Math.abs(eye.w) < 1e-8f) {
            return null;
        }
        return new Frame(viewCopy, vp, inverse);
    }
}
