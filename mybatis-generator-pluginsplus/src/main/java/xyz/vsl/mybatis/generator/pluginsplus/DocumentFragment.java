package xyz.vsl.mybatis.generator.pluginsplus;

import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Lokhov
 */
class DocumentFragment extends Element {
    private List<Element> list = new ArrayList<Element>();

    public DocumentFragment() {
    }

    public DocumentFragment(XmlElement parent) {
        this();
        list.addAll(parent.getElements());
    }

    @Override
    public String getFormattedContent(int i) {
        StringBuilder sb = new StringBuilder();
        for (Element e : list)
            sb.append(e.getFormattedContent(i));
        return sb.toString();
    }

    public void addElement(Element e) {
        list.add(e);
    }

    public List<Element> getElements() {
        return list;
    }

}
