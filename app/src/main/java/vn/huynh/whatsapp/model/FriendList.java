package vn.huynh.whatsapp.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duong on 4/15/2019.
 */

public class FriendList {
    private String id;
    private String name;
    private List<String> member = new ArrayList<>();

    public FriendList() {

    }

    public FriendList(String id) {
        this.id = id;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMember() {
        return member;
    }

    public void setMember(List<String> member) {
        this.member = member;
    }
}
