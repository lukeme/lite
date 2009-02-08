package org.xidea.lite.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xidea.el.json.JSONEncoder;
import org.xidea.el.operation.TextContains;
import org.xidea.lite.Template;

public class HTMLFormNodeParser extends HTMLNodeParser implements NodeParser {


	private static final String EL_INPUT = "input";
	private static final String EL_TEXTAREA = "textarea";
	private static final String EL_SELECT = "select";
	private static final String EL_OPTION = "option";
	private static final String ATTRIBUTE_SELECTED = "selected";
	private static final String ATTRIBUTE_CHECKED = "checked";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_VALUE = "value";

	private static final String TYPE_CHECKBOX = "checkbox";
	private static final String TYPE_RADIO = "radio";
	private static final Object KEY_SELECT = new Object();

	public HTMLFormNodeParser(XMLParser parser, boolean autoFillForm) {
		super(parser, autoFillForm);
	}

	protected Node parse(Node node, ParseContext context){
		Element el = (Element) node;
		String localName = el.getLocalName();
		if (EL_INPUT.equals(localName)) {
			return parseInput(el, context);
		} else if (EL_TEXTAREA.equals(localName)) {
			return parseTextArea(el, context);
		} else if (EL_SELECT.equals(localName)) {
			return parseSelect(el, context);
		} else if (EL_OPTION.equals(localName)) {
			return parseSelectOption(el, context);
		}else{
			return parseHTMLElement(node, context, null);
		}
	}


	protected Node parseSelect(Element el, ParseContext context) {
		context.setAttribute(KEY_SELECT, el);
		return parseHTMLElement(el, context, null);
	}

	protected Node parseInput(Element element, ParseContext context) {
		element = (Element) element.cloneNode(true);// options 有值，textarea有值
		String type = element.getAttribute(ATTRIBUTE_TYPE);
		if (TYPE_CHECKBOX.equals(type) || TYPE_RADIO.equals(type)) {
			if (element.hasAttribute(ATTRIBUTE_CHECKED)) {
				return parseHTMLElement(element, context, null);
			} else if (element.hasAttribute(ATTRIBUTE_NAME)
					&& element.hasAttribute(ATTRIBUTE_VALUE)) {
				String name = element.getAttribute(ATTRIBUTE_NAME);
				String value = element.getAttribute(ATTRIBUTE_VALUE);
				List<Object> attributes = buildCheckedAttribute(context,
						name, value, ATTRIBUTE_CHECKED);
				return parseHTMLElement(element, context, attributes);
			}
		} else {
			if (!element.hasAttribute(ATTRIBUTE_VALUE)
					&& element.hasAttribute(ATTRIBUTE_NAME)) {
				element.setAttribute(ATTRIBUTE_VALUE, "${"
						+ element.getAttribute(ATTRIBUTE_NAME) + "}");
				return parseHTMLElement(element, context, null);
			}
		}
		return parseHTMLElement(element, context, null);
	}


	protected Node parseTextArea(Element el, ParseContext context) {
		Document document = el.getOwnerDocument();
		if (el.hasAttribute(ATTRIBUTE_VALUE)) {
			el = (Element) el.cloneNode(false);
			String value = el.getAttribute(ATTRIBUTE_VALUE);
			el.removeAttribute(ATTRIBUTE_VALUE);
			el.appendChild(document.createTextNode(value));
		} else {
			el = (Element) el.cloneNode(true);
			Node child = el.getFirstChild();
			String name = el.getAttribute(ATTRIBUTE_NAME);
			if (child == null) {
				if (name.length() > 0) {
					el.appendChild(document.createTextNode("${" + name + "}"));
				}
			} else if (child.getNextSibling() == null) {
				if (child instanceof Text) {
					String value = ((Text) child).getData().trim();
					if (value.length() == 0 && name.length() > 0) {
						el.appendChild(document.createTextNode("${" + name
								+ "}"));
					}
				}
			}
		}
		return parseHTMLElement(el, context, null);
	}

	protected Node parseSelectOption(Element element, ParseContext context) {
		element = (Element) element.cloneNode(true);// options 有值，textarea有值
		Element selectNode = (Element) context.getAttribute(KEY_SELECT);
		if (!element.hasAttribute(ATTRIBUTE_SELECTED)) {
			if (selectNode.hasAttribute(ATTRIBUTE_NAME)
					&& selectNode.hasAttribute(ATTRIBUTE_VALUE)) {
				String name = selectNode.getAttribute(ATTRIBUTE_NAME);
				String value = selectNode.getAttribute(ATTRIBUTE_VALUE);
				List<Object> attributes = buildCheckedAttribute(context,
						name, value, ATTRIBUTE_SELECTED);
				return parseHTMLElement(element, context, attributes);
			}
		}
		return parseHTMLElement(element, context, null);
	}

	protected List<Object> buildCheckedAttribute(ParseContext context,
			String name, String value, String attributeName) {
		List<Object> attributes = new ArrayList<Object>();
		final String valueEL;
		if (value.startsWith("${") && value.endsWith("}")) {
			value = value.substring(2, value.length() - 1);
			valueEL = value;
		} else {
			valueEL = JSONEncoder.encode(value);
		}
		final String collectionEL = name;
		attributes
				.add(new Object[] {
						Template.IF_TYPE,
						this.parser.optimizeEL(buildCSEL(context, collectionEL,
								valueEL)) });
		attributes.add(" " + attributeName + "=\""
				+ BOOLEAN_ATTBUTE_MAP.get(attributeName) + "\"");
		attributes.add(END);
		return attributes;
	}

	protected String buildCSEL(ParseContext context, final String collectionEL,
			final String valueEL) {
		String id = context.addGlobalInvocable(TextContains.class);
		return id + "(" + collectionEL + "," + valueEL + ")";
	}

}
