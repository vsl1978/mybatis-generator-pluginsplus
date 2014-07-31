package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * @author Vladimir Lokhov
 */
class XmlTraverser<T> {

    private enum ActionType { add, delete, replace };
    private static class Action {
        ActionType type;
        XmlElement parent;
        int pos;
        Element e;
        private Action(ActionType type, XmlElement parent, int pos, Element e) {
            this.type = type;
            this.parent = parent;
            this.pos = pos;
            this.e = e;
        }
    }
    private List<Action> scheduled;

    public void start(XmlElement element) {
    }

    public void finish(XmlElement element) {
    }

    public void text(TextElement text) {
    }

    public void commitStructModifications() {
        //IdentityHashMap<XmlElement, List<Integer>> offsetChanges
        if (scheduled != null)
            for (Action a : scheduled) {
                int childrenCount = Objects.nvl(a.parent.getElements(), Collections.EMPTY_LIST).size();
                if (a.type == ActionType.add && a.pos >= childrenCount)
                    a.parent.addElement(a.e);
                else if (a.type == ActionType.add)
                    a.parent.addElement(a.pos, a.e);
                else if (a.type == ActionType.delete && a.pos != Integer.MAX_VALUE)
                    a.parent.getElements().remove(a.pos);
                else if (a.type == ActionType.delete)
                    a.parent.getElements().remove(a.e);
                else if (a.type == ActionType.replace && a.pos < childrenCount)
                    a.parent.getElements().set(a.pos, a.e);
            }
    }

    public void addLater(XmlElement parent, int pos, Element e) {
        if (scheduled == null) scheduled = new ArrayList<Action>();
        scheduled.add(new Action(ActionType.add, parent, pos, e));
    }

    public void addLater(XmlElement parent,  Element e) {
        if (scheduled == null) scheduled = new ArrayList<Action>();
        scheduled.add(new Action(ActionType.add, parent, Integer.MAX_VALUE, e));
    }

    public void removeLater(XmlElement parent, int pos) {
        if (scheduled == null) scheduled = new ArrayList<Action>();
        scheduled.add(new Action(ActionType.delete, parent, pos, null));
    }

    public void removeLater(XmlElement parent, Element e) {
        if (scheduled == null) scheduled = new ArrayList<Action>();
        scheduled.add(new Action(ActionType.delete, parent, Integer.MAX_VALUE, e));
    }

    public void replaceLater(XmlElement parent, int pos, Element e) {
        if (scheduled == null) scheduled = new ArrayList<Action>();
        scheduled.add(new Action(ActionType.replace, parent, pos, e));
    }

    public T result() {
        return null;
    }

}
