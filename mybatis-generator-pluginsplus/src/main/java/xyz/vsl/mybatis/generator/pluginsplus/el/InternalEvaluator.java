package xyz.vsl.mybatis.generator.pluginsplus.el;

import java.util.*;

/**
 * @author Vladimir Lokhov
 */
public class InternalEvaluator implements Evaluator {
    private Map<String, Operator> operators = new HashMap<String, Operator>();
    private Map<String, Function> functions = new HashMap<String, Function>();

    private Tokenizer tokenizer;
    private List<Token> compiledPostfix;

    public InternalEvaluator(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public Evaluator addFunction(Function fn) {
        functions.put(fn.getName(), fn);
        return this;
    }

    @Override
    public Evaluator addOperator(Operator op) {
        operators.put(op.getName(), op);
        if (op.getAltNames() != null)
            for (String name : op.getAltNames())
                operators.put(name, op);
        return this;
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public Evaluator compile(String expression) {
        //System.out.println("expression='"+expression+"'");
        List<String> infix = tokenizer.tokenize(expression);
        //System.out.println("tokenized expression="+infix);
        Stack<Token>   opstack = new Stack<Token>();
        Stack<Boolean> wrstack = new Stack<Boolean>();
        Stack<Integer> acstack = new Stack<Integer>();
        List<Token>    out     = new ArrayList<Token>();
        int n = 0;
        for (String token : infix) {
            n++;
            //System.out.println("Token #"+n+" '"+token+"' - "+opstack+", out: "+out);
            String _token = token.toLowerCase();
            if (operators.containsKey(_token)) {
                Operator o = operators.get(_token);
                Token t = new Token(Type.op, o.getName(), o.getArgsCount(), o.getPriority());
                while (!opstack.empty() && opstack.peek().priority >= t.priority)
                    out.add(opstack.pop());
                opstack.push(t);
            }
            else if (functions.containsKey(_token)) {
                Function f = functions.get(_token);
                Token t = new Token(Type.fn, f.getName());
                opstack.push(t);
                acstack.push(0);
                if (!wrstack.isEmpty()) {
                    wrstack.pop();
                    wrstack.push(true);
                }
                wrstack.push(false);
            }
            else if ("(".equals(token)) {
                if (!opstack.isEmpty() && opstack.peek().type == Type.fn && acstack.peek().intValue() == 0)
                    opstack.peek().setFnWithParentheses(true);
                opstack.push(new Token(Type.special, token));
            }
            else if (",".equals(token)) {
                while (!opstack.isEmpty()) {
                    Token t = opstack.peek();
                    if (t.type == Type.special && "(".equals(t.text) || t.type == Type.fn) {
                        break;
                    } else {
                        out.add(opstack.pop());
                    }
                }
                if (!opstack.isEmpty()) {
                    Boolean wr = wrstack.pop();
                    if (wr)
                        acstack.push(acstack.pop() + 1);
                    wrstack.push(false);
                } else throw new IllegalArgumentException("Invalid token #"+n+" - ','");
            }
            else if (")".equals(token)) {
                while (!opstack.isEmpty()) {
                    Token t = opstack.peek();
                    if (t.type == Type.special && "(".equals(t.text) || t.type == Type.fn && t.isFnWithParentheses()) {
                        break;
                    } else {
                        out.add(opstack.pop());
                    }
                }
                Token t = opstack.peek();
                if (t.type == Type.special) {
                    opstack.pop();
                    if (!opstack.isEmpty()) t = opstack.peek();
                }
                if (t.type == Type.fn) {
                    int count = acstack.pop();
                    if (wrstack.pop())
                        count++;
                    out.add(new Token(opstack.pop(), count));
                }
            }
            else {
                if (token.startsWith(Context.VAR_PREFIX))
                    out.add(new Token(Type.var, token.substring(Context.VAR_PREFIX.length())));
                else
                    out.add(new Token(Type.val, tokenizer.unescape(token)));
                if (!wrstack.isEmpty()) {
                    wrstack.pop();
                    wrstack.push(true);
                }
            }
        }
        if (!opstack.isEmpty() && opstack.peek().type == Type.special)
            throw new IllegalArgumentException("Stack state: "+opstack+", output: "+out);

        while (!opstack.empty()) {
            Token t = opstack.peek();
            if (t.type == Type.fn) {
                int count = acstack.pop();
                if (wrstack.pop())
                    count++;
                out.add(new Token(opstack.pop(), count));
            }
            else out.add(opstack.pop());
        }

        compiledPostfix = out;
        //System.out.println("Compiled expression: "+out);
        return this;
    }

    @Override
    public String evaluate(Context context) {
        List<Token> postfix = new ArrayList<Token>();
        postfix.addAll(compiledPostfix);

        Stack<Token> stack = new Stack<Token>();
        for (Token token : postfix) {
            if (token.type == Type.op || token.type == Type.fn) {
                List<Token> args = new ArrayList<Token>();
                for (int i = 0; i < token.argsCount; i++)
                    args.add(0, stack.pop());
                Action action = null;
                if (token.type == Type.op)
                    action = operators.get(token.text);
                else if (token.type == Type.fn) {
                    action = context.fn(token.text);
                    if (action == null)
                        action = functions.get(token.text);
                }
                String result = action.apply(context, args);
                stack.push(new Token(Type.val, result));
            } else {
                stack.push(token);
            }
        }
        return context.resolve(stack.pop());
    }
}
