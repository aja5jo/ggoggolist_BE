package group5.backend.service.ai.recomm;

public final class VectorMath {
    private VectorMath(){}

    public static double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dot=0, na=0, nb=0;
        for (int i=0;i<a.length;i++){ dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i]; }
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public static float[] l2normalize(float[] v) {
        double n=0; for (float x: v) n += x*x;
        if (n == 0) return v;
        float inv = (float)(1.0/Math.sqrt(n));
        float[] out = new float[v.length];
        for (int i=0;i<v.length;i++) out[i] = v[i] * inv;
        return out;
    }
}
