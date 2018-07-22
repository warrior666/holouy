package org.huy.tools.util;

import com.alfresco.client.api.authentication.representation.TicketRepresentation;
import com.alfresco.client.api.authentication.representation.ValidTicketRepresentation;
import com.alfresco.client.api.common.deserializer.EntryDeserializer;
import com.alfresco.client.api.common.deserializer.PagingDeserializer;
import com.alfresco.client.api.common.representation.ResultPaging;
import com.alfresco.client.api.core.model.deserializer.FavoriteEntryDeserializer;
import com.alfresco.client.api.core.model.representation.ActivityRepresentation;
import com.alfresco.client.api.core.model.representation.CommentRepresentation;
import com.alfresco.client.api.core.model.representation.DeletedNodeRepresentation;
import com.alfresco.client.api.core.model.representation.FavoriteRepresentation;
import com.alfresco.client.api.core.model.representation.GroupMemberRepresentation;
import com.alfresco.client.api.core.model.representation.GroupRepresentation;
import com.alfresco.client.api.core.model.representation.NodeRepresentation;
import com.alfresco.client.api.core.model.representation.PersonRepresentation;
import com.alfresco.client.api.core.model.representation.PreferenceRepresentation;
import com.alfresco.client.api.core.model.representation.RatingRepresentation;
import com.alfresco.client.api.core.model.representation.RenditionRepresentation;
import com.alfresco.client.api.core.model.representation.SharedLinkRepresentation;
import com.alfresco.client.api.core.model.representation.SiteContainerRepresentation;
import com.alfresco.client.api.core.model.representation.SiteMemberRepresentation;
import com.alfresco.client.api.core.model.representation.SiteMembershipRequestRepresentation;
import com.alfresco.client.api.core.model.representation.SiteRepresentation;
import com.alfresco.client.api.core.model.representation.SiteRoleRepresentation;
import com.alfresco.client.api.core.model.representation.TagRepresentation;
import com.alfresco.client.api.core.model.representation.VersionRepresentation;
import com.alfresco.client.api.discovery.model.RepositoryInfoRepresentation;
import com.alfresco.client.api.search.deserializer.ResultSetPagingDeserializer;
import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.ResultSetRepresentation;
import com.alfresco.client.utils.ISO8601Utils;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class GsonHelper
{
    public static GsonBuilder getDefaultGsonBuilder()
    {
        return new GsonBuilder().setDateFormat(ISO8601Utils.DATE_ISO_FORMAT)
                // Entry
                .registerTypeAdapter(ActivityRepresentation.class, new EntryDeserializer<ActivityRepresentation>())
                .registerTypeAdapter(CommentRepresentation.class, new EntryDeserializer<CommentRepresentation>())
                .registerTypeAdapter(SiteMemberRepresentation.class,
                        new EntryDeserializer<SiteMemberRepresentation>())
                .registerTypeAdapter(NodeRepresentation.class, new EntryDeserializer<NodeRepresentation>())
                .registerTypeAdapter(DeletedNodeRepresentation.class,
                        new EntryDeserializer<DeletedNodeRepresentation>())
                .registerTypeAdapter(PreferenceRepresentation.class,
                        new EntryDeserializer<PreferenceRepresentation>())
                .registerTypeAdapter(RatingRepresentation.class, new EntryDeserializer<RatingRepresentation>())
                .registerTypeAdapter(PersonRepresentation.class, new EntryDeserializer<PersonRepresentation>())
                .registerTypeAdapter(SharedLinkRepresentation.class,
                        new EntryDeserializer<SharedLinkRepresentation>())
                .registerTypeAdapter(RenditionRepresentation.class,
                        new EntryDeserializer<RenditionRepresentation>())
                .registerTypeAdapter(SiteContainerRepresentation.class,
                        new EntryDeserializer<SiteContainerRepresentation>())
                .registerTypeAdapter(SiteRoleRepresentation.class, new EntryDeserializer<SiteRoleRepresentation>())
                .registerTypeAdapter(SiteMembershipRequestRepresentation.class,
                        new EntryDeserializer<SiteMembershipRequestRepresentation>())
                .registerTypeAdapter(SiteRepresentation.class, new EntryDeserializer<SiteRepresentation>())
                .registerTypeAdapter(TagRepresentation.class, new EntryDeserializer<TagRepresentation>())
                .registerTypeAdapter(VersionRepresentation.class, new EntryDeserializer<VersionRepresentation>())
                .registerTypeAdapter(FavoriteRepresentation.class, new FavoriteEntryDeserializer())

                // Paging Results
                .registerTypeAdapter(new TypeToken<ResultPaging<ActivityRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(ActivityRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<CommentRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(CommentRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<SiteMemberRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(SiteMemberRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<SharedLinkRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(SharedLinkRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<NodeRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(NodeRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<DeletedNodeRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(DeletedNodeRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<PreferenceRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(PreferenceRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<FavoriteRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(FavoriteRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<RatingRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(RatingRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<RenditionRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(RenditionRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<SiteContainerRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(SiteContainerRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<SiteRoleRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(SiteRoleRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<SiteMembershipRequestRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(SiteMembershipRequestRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<SiteRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(SiteRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<TagRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(TagRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<VersionRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(VersionRepresentation.class))

                // People API
                .registerTypeAdapter(PersonRepresentation.class, new EntryDeserializer<PersonRepresentation>())
                .registerTypeAdapter(new TypeToken<ResultPaging<PersonRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(PersonRepresentation.class))

                // Groups API
                .registerTypeAdapter(GroupRepresentation.class, new EntryDeserializer<GroupRepresentation>())
                .registerTypeAdapter(GroupMemberRepresentation.class,
                        new EntryDeserializer<GroupMemberRepresentation>())

                .registerTypeAdapter(new TypeToken<ResultPaging<GroupRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(GroupRepresentation.class))
                .registerTypeAdapter(new TypeToken<ResultPaging<GroupMemberRepresentation>>()
                {
                }.getType(), new PagingDeserializer<>(GroupMemberRepresentation.class))

                // Authentication
                .registerTypeAdapter(TicketRepresentation.class, new EntryDeserializer<TicketRepresentation>())
                .registerTypeAdapter(ValidTicketRepresentation.class,
                        new EntryDeserializer<ValidTicketRepresentation>())

                // Discovery
                .registerTypeAdapter(RepositoryInfoRepresentation.class,
                        new EntryDeserializer<RepositoryInfoRepresentation>())

                // Search
                .registerTypeAdapter(ResultNodeRepresentation.class,
                        new EntryDeserializer<ResultNodeRepresentation>())
                .registerTypeAdapter(new TypeToken<ResultSetRepresentation<ResultNodeRepresentation>>()
                {
                }.getType(), new ResultSetPagingDeserializer<>(ResultNodeRepresentation.class))

        // Workflow

        ;
    }
}
