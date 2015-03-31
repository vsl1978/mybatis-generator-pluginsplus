package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Lokhov
 */
public class DefaultTokenizer implements Tokenizer {
    final static int C_ANY   = -1;
    final static int C_SPACE = 0;
    final static int C_ALNUM = 1;
    final static int C_ALONE = 2;
    final static int C_SPEC  = 3;

    private static class State {
        List<String> out = new ArrayList<String>();
        int prevCharType = C_SPACE;
        boolean escape;
        boolean literal;
        char endOfLiteral = ' ';

        String pop(StringBuilder sb) {
            String s = sb.toString();
            sb.setLength(0);
            return s;
        }

        void add(StringBuilder sb) {
            if (sb.length() > 0)
                out.add(pop(sb));
        }
    }

    @Override
    public List<String> tokenize(String expression) {
        return tokenize0(expression).out;
    }

    private State tokenize0(String expression) {
        State state = new State();
        char[] str = expression.toCharArray();

        StringBuilder sb = new StringBuilder();
        for (char c : str) {
            if (state.escape) {
                state.escape = false;
                sb.append(c);
                state.prevCharType = C_ANY;
            }
            else if (c == '\\') {
                state.escape = true;
                sb.append(c);
            }
            else if (state.literal) {
                if (c == state.endOfLiteral) {
                    state.add(sb);
                    state.literal = false;
                    state.prevCharType = C_SPACE;
                } else sb.append(c);
            }
            else if (c == '"' || c == '/' || c == '\'') {
                state.literal = true;
                state.endOfLiteral = c;
            }
            else {
                int charType = (c <= ' ') ? C_SPACE : "(,)!".indexOf(c) >= 0 ? C_ALONE : "&|=<>!~^#%@+".indexOf(c) >= 0 ? C_SPEC : C_ALNUM;

                if (charType != state.prevCharType && state.prevCharType != C_ANY || state.prevCharType == C_ALONE || charType == C_ALONE)
                    state.add(sb);
                if (charType != C_SPACE) {
                    if (c == '`')
                        c = '"';
                    sb.append(c);
                }
                if (charType == C_ALONE)
                    state.add(sb);
                state.prevCharType = charType;
            }
        }
        state.add(sb);
        return state;
    }

    @Override
    public String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        if (text != null && text.length() > 0) {
            char[] str = text.toCharArray();
            boolean escaped = false;
            for (char c : str) {
                if (escaped) {
                    sb.append(c);
                    escaped = false;
                }
                else if (c == '\\') {
                    escaped = true;
                }
                else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String escape(String text) {
        StringBuilder sb = new StringBuilder();
        if (text != null && text.length() > 0) {
            char[] str = text.toCharArray();
            boolean escaped = false;
            for (int i = 0; i < str.length; i++) {
                char c = str[i];
                if (c <= ' ' || "\"'/(,)&|=<>!~^#%@+".indexOf(c) >= 0) {
                    sb.append('\\').append(c);
                    escaped = false;
                } else if (c == '\\') {
                    if (escaped) {
                        sb.append(c);
                        escaped = false;
                    } else {
                        if (i < str.length - 1 && str[i+1] == '`')
                            sb.append(c);
                        else {
                            sb.append('\\').append(c);
                            escaped = true;
                        }
                    }
                } else {
                    sb.append(c);
                    escaped = false;
                }
            }
        }
        return sb.toString();
    }
}
