package com.googlecode.totallylazy.xml;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Xml;
import com.googlecode.totallylazy.iterators.StatefulIterator;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import java.io.StringReader;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.memorise;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.totallylazy.xml.StreamingXPath.descendant;
import static com.googlecode.totallylazy.xml.StreamingXPath.name;
import static com.googlecode.totallylazy.xml.XmlReader.xmlReader;
import static org.hamcrest.MatcherAssert.assertThat;

public class XmlReaderTest {
    @Test
    public void canStreamIntoAMap() throws Exception {
        String xml = "<stream><user><first>Dan</first><dob>1977</dob></user><user><first>Jason</first><dob>1978</dob></user></stream>";
        Sequence<Location> locations =  memorise(xmlReader(new StringReader(xml)).iterator(descendant(name("user"))));
        Sequence<Map<String, String>> users = locations.map(user -> {
            StatefulIterator<Location> iterator = user.stream().iterator(descendant(name("first").or(name("dob"))));
            return map(memorise(iterator).map(
                    field -> pair(field.current().getName().getLocalPart(), new TextValue().call(field))));
        }).memoize();
        assertThat(users, hasExactly(map("first", "Dan", "dob", "1977"), map("first", "Jason", "dob", "1978")));
    }

    @Test
    public void currentlyItEscapesCData() throws Exception {
        String xml = "<stream><![CDATA[Hello <> ]]></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "stream"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.head()), is("<stream>Hello &lt;&gt; </stream>"));
    }

    @Test
    public void copiesAllText() throws Exception {
        String xml = "<stream>Hello &amp; World</stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "stream"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.head()), is("<stream>Hello &amp; World</stream>"));
    }

    @Test
    public void emptyRoot() throws Exception {
        String xml = "<stream/>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "stream"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.head()), is("<stream/>"));
    }

    @Test
    public void singleItem() throws Exception {
        String xml = "<stream><item/></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.head()), is("<item/>"));
    }

    @Test
    public void twoItems() throws Exception {
        String xml = "<stream><item/><item/></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(2));
        assertThat(Xml.asString(stream.first()), is("<item/>"));
        assertThat(Xml.asString(stream.second()), is("<item/>"));

    }

    @Test
    public void oneItemWithChild() throws Exception {
        String xml = "<stream><item><child/></item></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.head()), is("<item><child/></item>"));
    }

    @Test
    public void oneItemWithTwoChildren() throws Exception {
        String xml = "<stream><item><child/><child/></item></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.head()), is("<item><child/><child/></item>"));
    }

    @Test
    public void twoItemsWithChild() throws Exception {
        String xml = "<stream><item><child/></item><item><child/></item></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(2));
        assertThat(Xml.asString(stream.first()), is("<item><child/></item>"));
        assertThat(Xml.asString(stream.second()), is("<item><child/></item>"));
    }

    @Test
    public void twoItemsWithTwoChild() throws Exception {
        String xml = "<stream><item><child/><child/></item><item><child/><child/></item></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(2));
        assertThat(Xml.asString(stream.first()), is("<item><child/><child/></item>"));
        assertThat(Xml.asString(stream.second()), is("<item><child/><child/></item>"));
    }

    @Test
    public void supportsAttributes() throws Exception {
        String xml = "<stream><item foo='bar'/></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(1));
        assertThat(Xml.asString(stream.first()), is("<item foo=\"bar\"/>"));
    }

    @Test
    public void supportsAttributesOnNestedStructures() throws Exception {
        String xml = "<stream><item><child foo='bar'/><child/></item><item><child/><child baz='bar'/></item></stream>";
        Sequence<Node> stream = memorise(xmlReader(new StringReader(xml), "item"));
        assertThat(stream.size(), is(2));
        assertThat(Xml.asString(stream.first()), is("<item><child foo=\"bar\"/><child/></item>"));
        assertThat(Xml.asString(stream.second()), is("<item><child/><child baz=\"bar\"/></item>"));
    }
}
