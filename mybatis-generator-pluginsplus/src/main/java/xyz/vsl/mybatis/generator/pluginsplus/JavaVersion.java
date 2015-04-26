package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * @author Vladimir Lokhov
 */
enum JavaVersion {
    java5("java15", "15", "5"),
    java6("java16", "16", "6"),
    java7("java17", "17", "7"),
    java8("java18", "18", "8");

    private final String[] aliases;

    JavaVersion(String ... aliases) {
        this.aliases = aliases;
    }

    public boolean isSubsetOf(JavaVersion version) {
        return this.ordinal() <= version.ordinal();
    }

    public static JavaVersion parse(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c))
                sb.append(Character.toLowerCase(c));
        }
        s = sb.toString();
        for (JavaVersion jv : values()) {
            if (s.equals(jv.name()))
                return jv;
            for (String v : jv.aliases)
                if (s.equals(v))
                    return jv;
        }
        return null;
    }
}
