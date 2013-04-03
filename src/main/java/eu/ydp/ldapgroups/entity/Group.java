package eu.ydp.ldapgroups.entity;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "`group`",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Group implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Column(name = "name", nullable = false)
    String name;

    @ElementCollection
    @CollectionTable(name = "group_member", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "member")
    Set<String> members;

    @Column(name = "date_created", nullable = false)
    Date dateCreated;

    @Column(name = "date_modified", nullable = false)
    Date dateModified;

    @Column(name = "date_synchronized", nullable = true)
    Date dateSynchronized;

    public Group(long id, String name, Set<String> members, Date dateModified, Date dateSynchronized) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.dateModified = dateModified;
        this.dateSynchronized = dateSynchronized;
    }

    public Group() {
        this(0, null, null, null, null);
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getMembers() {
        return members;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public Date getDateSynchronized() {
        return dateSynchronized;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", modified='" + (dateModified!=null ? dateModified.getTime() : null) + '\'' +
                ", synchronized='" + (dateSynchronized!=null ? dateSynchronized.getTime() : null) + '\'' +
                '}';
    }

    @Override
    public Group clone() {
        try {
            return (Group)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    static public class Builder {
        Group group;

        public Builder(Group initialData) {
            this.group = initialData;

            dateModified(new Date());
        }

        public Builder() {
            this(new Group());
        }

        public Builder name(String name) {
            group.name = name;
            return this;
        }
        public Builder members(Set<String> members) {
            group.members = members;
            return this;
        }
        public Builder members(String... members) {
            group.members = new HashSet<String>(Arrays.asList(members));
            return this;
        }
        public Builder dateSynchronizedFromModified() {
            group.dateSynchronized = group.getDateModified();
            return this;
        }
        public Builder dateModified(Date date) {
            if (group.dateCreated==null)
                group.dateCreated = date;
            group.dateModified = date;
            return this;
        }
        public Builder dateModified() {
            return dateModified(new Date());
        }
        public Group build() {
            return group;
        }
    }
}
