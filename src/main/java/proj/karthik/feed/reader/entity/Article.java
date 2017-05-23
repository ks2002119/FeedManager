package proj.karthik.feed.reader.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.sql.Timestamp;
import java.util.Optional;

import static proj.karthik.feed.reader.Constants.ARTICLE;
import static proj.karthik.feed.reader.Constants.BODY;
import static proj.karthik.feed.reader.Constants.CREATED_ON;
import static proj.karthik.feed.reader.Constants.TITLE;


/**
 * Entity class representing Article
 */
@JsonRootName(ARTICLE)
public class Article {

    private String title;
    private String body;
    private Optional<Timestamp> createdOn;

    public Article() {
    }

    public Article(final String title, final String body) {
        this.title = title;
        this.body = body;
    }

    public Article(final String title, final String body, final Timestamp createdOn) {
        this.title = title;
        this.body = body;
        this.createdOn = Optional.of(createdOn);
    }

    @JsonGetter(TITLE)
    public String getTitle() {
        return title;
    }

    @JsonSetter(TITLE)
    public void setTitle(final String title) {
        this.title = title;
    }

    @JsonGetter(BODY)
    public String getBody() {
        return body;
    }

    @JsonSetter(BODY)
    public void setBody(String body) {
        this.body = body;
    }

    @JsonSetter(CREATED_ON)
    public void setCreatedOn(final Timestamp createdOn) {
        this.createdOn = Optional.of(createdOn);
    }

    @JsonGetter(CREATED_ON)
    public Timestamp getCreatedOn() {
        if (createdOn != null) {
            return createdOn.get();
        } else {
            return null;
        }
    }
}
