package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

/**
 * @author Vladimir Lokhov
 */
class Objects {

    public static boolean equals(Object a, Object b) {
        return a == null && b == null || a != null && a.equals(b);
    }

    public static <T> T nvl(T a, T b) {
        return a != null ? a : b;
    }
    public static <T> T nvl(T a, T b, T c) {
        return a != null ? a : b != null ? b : c;
    }


    public static int findAnnotaion(List<String> annotations, String annotationClassName) {
        return findAnnotaion(annotations, annotationClassName, null);
    }

    public static int findAnnotaion(List<String> annotations, String annotationClassName, String annotationClassSimpleName) {
        if (annotations == null || annotations.isEmpty())
            return -1;
        if (annotationClassName == null || (annotationClassName = annotationClassName.trim()).length() == 0)
            return -1;
        if (annotationClassSimpleName == null || annotationClassSimpleName.length() == 0) {
            int lastdot = annotationClassName.lastIndexOf('.');
            int last$ = annotationClassName.lastIndexOf('$');
            annotationClassSimpleName = annotationClassName.substring(Math.max(lastdot, last$) + 1);
        }
        int idx = -1;
        for (String a : annotations) {
            idx++;
            if (a == null)
                continue;
            int pos = a.indexOf('@');
            if (pos < 0)
                continue;
            a = a.substring(pos + 1).trim(); // strip '@'
            if (a.length() < annotationClassSimpleName.length())
                continue;
            String s;
            if (!a.startsWith(s = annotationClassName) && !a.startsWith(s = annotationClassSimpleName))
                continue;
            int next = s.length();
            if (a.length() <= next)
                return idx;
            char c = a.charAt(next);
            if (c <= ' ' || c == '(')
                return idx;
        }
        return -1;
    }

}
