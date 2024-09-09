package me.bannock.website.services.blog;

public record Comment(long authorId, long millisPosted, String content) {
}
