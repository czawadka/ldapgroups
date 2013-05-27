package eu.ydp.ldapgroups.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(
        name = "`group`",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Group implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_member", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "member")
    Set<String> members;

    @Column(name = "date_created", nullable = false)
    Date dateCreated;

    @Column(name = "date_modified", nullable = false)
    Date dateModified;

    @Column(name = "date_synchronized", nullable = true)
    Date dateSynchronized;

    @Column(name = "sync_error", nullable = true)
    SyncError syncError;

    @Column(name = "sync_description", nullable = true)
    String syncDescription;

    public Group(Long id, String name, Set<String> members, Date dateModified, Date dateSynchronized) {
        this.id = id;
        this.name = name;
        this.members = members!=null ? members : Collections.EMPTY_SET;
        this.dateModified = dateModified;
        this.dateSynchronized = dateSynchronized;
    }

    public Group() {
        this(null, null, null, null, null);
    }


    public Long getId() {
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

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public Date getDateSynchronized() {
        return dateSynchronized;
    }

    public void setDateSynchronized(Date dateSynchronized) {
        this.dateSynchronized = dateSynchronized;
    }

    public SyncError getSyncError() {
        return syncError;
    }

    public void setSyncError(SyncError syncError) {
        this.syncError = syncError;
    }

    public String getSyncDescription() {
        return syncDescription;
    }

    public void setSyncDescription(String syncDescription) {
        this.syncDescription = syncDescription;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", members="+members +
                ", dateModified='" + (dateModified!=null ? dateModified.getTime() : null) + '\'' +
                ", dateSynchronized='" + (dateSynchronized!=null ? dateSynchronized.getTime() : null) + '\'' +
                ", syncError=" + syncError +
                ", syncDescription='" + syncDescription + '\'' +
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

        public Builder dateModified(long timestamp) {
            return dateModified(new Date(timestamp));
        }

        public Builder dateModified() {
            return dateModified(new Date());
        }

        public Builder id(long id) {
            group.id = id;
            return this;
        }

        public Builder sync(Date dateSynchronized, SyncError syncError, String syncDescription) {
            group.setDateSynchronized(dateSynchronized);
            group.setSyncError(syncError);
            group.setSyncDescription(syncDescription);
            return this;
        }

        public Group build() {
            return group;
        }
    }
}
