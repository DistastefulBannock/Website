package me.bannock.website.security;

public final class Roles {

    public static final String[] DEFAULT_ANON_ROLES = new String[]{
            StorageServiceRoles.LOAD_DATA,
            BlogServiceRoles.READ_POSTS,
            BlogServiceRoles.READ_COMMENTS
    };

    public static final String[] DEFAULT_USER_ROLES = new String[]{
            StorageServiceRoles.LOAD_DATA,
            BlogServiceRoles.READ_POSTS,
            BlogServiceRoles.READ_COMMENTS,
            BlogServiceRoles.MAKE_COMMENTS
    };

    public static final String[] DEFAULT_ADMIN_ROLES = new String[]{
            StorageServiceRoles.SAVE_DATA,
            StorageServiceRoles.LOAD_DATA,
            BlogServiceRoles.READ_POSTS,
            BlogServiceRoles.MAKE_POSTS,
            BlogServiceRoles.READ_COMMENTS,
            BlogServiceRoles.MAKE_COMMENTS
    };

    public static final class StorageServiceRoles{
        public static final String SAVE_DATA = "STORAGE_SAVE";
        public static final String LOAD_DATA = "STORAGE_LOAD";
    }

    public static final class BlogServiceRoles{
        public static final String READ_POSTS = "BLOG_READ_POSTS";
        public static final String MAKE_POSTS = "BLOG_MAKE_POSTS";
        public static final String READ_COMMENTS = "BLOG_READ_COMMENTS";
        public static final String MAKE_COMMENTS = "BLOG_MAKE_COMMENTS";
    }

}
