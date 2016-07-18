package org.jsoup.parser;
/**
 * 利用增加prefix的方式，騙過 Token.Read
 * 再於寫入StartTag及EndTag前，將TagName還原
 *
 * <p>Usage example: {@code Document xmlDoc = Jsoup.parse(html, baseUrl, new Parser( new PrefixXmlTreeBuilder("prefixName")  );}</p>
 *
 * @author Abola Lee<abola921@gmail.com>
 * @deprecated
 ****
 * Use the {@code XmlTreeBuilder} when you want to parse XML without any of the HTML DOM rules being applied to the
 * document.
 * <p>Usage example: {@code Document xmlDoc = Jsoup.parse(html, baseUrl, Parser.xmlParser());}</p>
 *
 * @author Jonathan Hedley
 */

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.*;
import org.jsoup.parser.Token.Comment;
import org.jsoup.parser.Token.Doctype;
import org.jsoup.parser.Token.EndTag;
import org.jsoup.parser.Token.StartTag;

import java.util.List;

public class PrefixXmlTreeBuilder extends XmlTreeBuilder {
    String prefix;
    public PrefixXmlTreeBuilder(String prefix) {
        this.prefix = prefix;
    }


    protected boolean process(Token token) {
        switch (token.type) {
            case StartTag:
                insert(token.asStartTag());
                break;
            case EndTag:
                popStackToClose(token.asEndTag());
                break;
            case Comment:
                insert(token.asComment());
                break;
            case Character:
                insert(token.asCharacter());
                break;
            case Doctype:
                insert(token.asDoctype());
                break;
            case EOF: // could put some normalisation here if desired
                break;
            default:
                Validate.fail("Unexpected token type: " + token.type);
        }

        return true;
    }


    protected void initialiseParse(String input, String baseUri, ParseErrorList errors) {
        super.initialiseParse(input, baseUri, errors);
        this.stack.add(this.doc);
        this.doc.outputSettings().syntax(Syntax.xml);
    }

    private void insertNode(Node node) {
        this.currentElement().appendChild(node);
    }

    Element insert(StartTag startTag) {
        // remove prefix
        Tag tag = Tag.valueOf(startTag.name().replace(this.prefix,""));
        Element el = new Element(tag, this.baseUri, startTag.attributes);
        this.insertNode(el);
        if(startTag.isSelfClosing()) {
            this.tokeniser.acknowledgeSelfClosingFlag();
            if(!tag.isKnownTag()) {
                tag.setSelfClosing();
            }
        } else {
            this.stack.add(el);
        }

        return el;
    }

    void insert(Comment commentToken) {

        org.jsoup.nodes.Comment comment = new org.jsoup.nodes.Comment(commentToken.getData(), this.baseUri);
        Object insert = comment;
        if(commentToken.bogus) {
            String data = comment.getData();
            if(data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                String declaration = data.substring(1);
                insert = new XmlDeclaration(declaration, comment.baseUri(), data.startsWith("!"));
            }
        }

        this.insertNode((Node)insert);
    }

    void insert(Token.Character characterToken) {
        TextNode node = new TextNode(characterToken.getData(), this.baseUri);
        this.insertNode(node);
    }

    void insert(Doctype d) {
        DocumentType doctypeNode = new DocumentType(d.getName(), d.getPublicIdentifier(), d.getSystemIdentifier(), this.baseUri);
        this.insertNode(doctypeNode);
    }

    private void popStackToClose(EndTag endTag) {
        // remove prefix
        String elName = endTag.name().replace(this.prefix,"");
        Element firstFound = null;

        int pos;
        Element next;
        for(pos = this.stack.size() - 1; pos >= 0; --pos) {
            next = (Element)this.stack.get(pos);
            if(next.nodeName().equals(elName)) {
                firstFound = next;
                break;
            }
        }

        if(firstFound != null) {
            for(pos = this.stack.size() - 1; pos >= 0; --pos) {
                next = (Element)this.stack.get(pos);
                this.stack.remove(pos);
                if(next == firstFound) {
                    break;
                }
            }

        }
    }

    List<Node> parseFragment(String inputFragment, String baseUri, ParseErrorList errors) {
        this.initialiseParse(inputFragment, baseUri, errors);
        this.runParser();
        return this.doc.childNodes();
    }
}
