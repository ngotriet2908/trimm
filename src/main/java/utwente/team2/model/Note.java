//package utwente.team2.model;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.annotation.JsonPropertyOrder;
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder({
//        "cardTypeName",
//        "text",
//        "name"
//})
//public class Note {
//
//    @JsonProperty("cardTypeName")
//    private String cardTypeName;
//    @JsonProperty("text")
//    private String text;
//    @JsonProperty("name")
//    private String name;
//
//    /**
//     * No args constructor for use in serialization
//     *
//     */
//    public Note() {
//
//    }
//
//    /**
//     *
//     * @param text
//     * @param cardTypeName
//     * @param name
//     */
//    public Note(String cardTypeName, String text, String name) {
//        super();
//        this.cardTypeName = cardTypeName;
//        this.text = text;
//        this.name = name;
//    }
//
//    @JsonProperty("cardTypeName")
//    public String getCardTypeName() {
//        return cardTypeName;
//    }
//
//    @JsonProperty("cardTypeName")
//    public void setCardTypeName(String cardTypeName) {
//        this.cardTypeName = cardTypeName;
//    }
//
//    @JsonProperty("text")
//    public String getText() {
//        return text;
//    }
//
//    @JsonProperty("text")
//    public void setText(String text) {
//        this.text = text;
//    }
//
//    @JsonProperty("name")
//    public String getName() {
//        return name;
//    }
//
//    @JsonProperty("name")
//    public void setName(String name) {
//        this.name = name;
//    }
//
//}


package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Note implements Card {

    private String cardTypeName;
    private String text;
    private String name;

    public Note() {

    }

    public Note(String cardTypeName, String text, String name) {
        this.cardTypeName = cardTypeName;
        this.text = text;
        this.name = name;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
