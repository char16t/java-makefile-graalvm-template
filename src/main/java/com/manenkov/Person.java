package com.manenkov;

public class Person {
    
    private String name;
    private Integer age;
    private Subp subp;

    public Subp getSubp() {
        return subp;
    }
    public void setSubp(Subp subp) {
        this.subp = subp;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
}
