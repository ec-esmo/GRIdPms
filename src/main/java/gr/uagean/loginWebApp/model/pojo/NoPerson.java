/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

/**
 *
 * @author nikos
 */
public class NoPerson {

    /*
    {
    "sub": "f9093e31-8592-4ebf-9925-7090579f79a0",
    "connect-userid_sec": [
        "eidas:$Mohamed$Al Samed$1990-08-19$SE/NO/199008199391"
    ],
    "dataporten-userid_sec": [
        "eidas:$Mohamed$Al Samed$1990-08-19$SE/NO/199008199391"
    ],
    "name": "Mohamed Al Samed",
    "picture": "https://api.dataporten-test.uninett.no/userinfo/v1/user/media/p:3cfe174c-b7b3-45e8-aea4-782a87aa492e"
}
     */
    private String sub;
    @JsonProperty("connect-userid_sec")
    @JsonAlias({"userid_sec"})
    private String[] connectUserIdSec;
    @JsonProperty("dataporten-userid_sec")
    private String[] dataportenUserIdSec;
    private String name;
    private String picture;
    private User user;

    public NoPerson() {
    }

    public NoPerson(String sub, String[] connectUserIdSec, String[] dataportenUserIdSec, String name, String picture, User user) {
        this.sub = sub;
        this.connectUserIdSec = connectUserIdSec;
        this.dataportenUserIdSec = dataportenUserIdSec;
        this.name = name;
        this.picture = picture;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String[] getConnectUserIdSec() {
        return connectUserIdSec;
    }

    public void setConnectUserIdSec(String[] connectUserIdSec) {
        this.connectUserIdSec = connectUserIdSec;
    }

    public String[] getDataportenUserIdSec() {
        return dataportenUserIdSec;
    }

    public void setDataportenUserIdSec(String[] dataportenUserIdSec) {
        this.dataportenUserIdSec = dataportenUserIdSec;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "NoPerson{" + "sub=" + sub + ", connectUserIdSec=" + Arrays.toString(connectUserIdSec) + ", dataportenUserIdSec=" + Arrays.toString(dataportenUserIdSec) + ", name=" + name + ", picture=" + picture + '}';
    }

    public class User {

        private String sub;
        @JsonProperty("connect-userid_sec")
        @JsonAlias({"userid_sec"})
        private String[] connectUserIdSec;
        @JsonProperty("dataporten-userid_sec")
        private String[] dataportenUserIdSec;
        private String name;
        private String picture;

        public User() {
        }

        public User(String sub, String[] connectUserIdSec, String[] dataportenUserIdSec, String name, String picture) {
            this.sub = sub;
            this.connectUserIdSec = connectUserIdSec;
            this.dataportenUserIdSec = dataportenUserIdSec;
            this.name = name;
            this.picture = picture;
        }

        public String getSub() {
            return sub;
        }

        public void setSub(String sub) {
            this.sub = sub;
        }

        public String[] getConnectUserIdSec() {
            return connectUserIdSec;
        }

        public void setConnectUserIdSec(String[] connectUserIdSec) {
            this.connectUserIdSec = connectUserIdSec;
        }

        public String[] getDataportenUserIdSec() {
            return dataportenUserIdSec;
        }

        public void setDataportenUserIdSec(String[] dataportenUserIdSec) {
            this.dataportenUserIdSec = dataportenUserIdSec;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        @Override
        public String toString() {
            return "NoPerson{" + "sub=" + sub + ", connectUserIdSec=" + Arrays.toString(connectUserIdSec) + ", dataportenUserIdSec=" + Arrays.toString(dataportenUserIdSec) + ", name=" + name + ", picture=" + picture + '}';
        }
    }

}
