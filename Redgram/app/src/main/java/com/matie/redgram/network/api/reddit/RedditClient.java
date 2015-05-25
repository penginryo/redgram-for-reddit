package com.matie.redgram.network.api.reddit;

import com.matie.redgram.managers.rxbus.RxBus;
import com.matie.redgram.models.PostItem;
import com.matie.redgram.models.events.SubredditEvent;
import com.matie.redgram.models.reddit.RedditLink;
import com.matie.redgram.network.api.reddit.base.RedditProviderBase;
import com.matie.redgram.network.api.reddit.base.RedditServiceBase;

import rx.Observable;

/**
 * Created by matie on 17/04/15.
 */
public class RedditClient extends RedditServiceBase {

    private static RedditClient clientInstance = null;

    private final RedditProviderBase provider;

    private RedditClient(boolean isAuth) {
        if(isAuth)
            provider = getRestAdapter(OAUTH_URL).create(RedditProviderBase.class);
        else
            provider = getRestAdapter(REDDIT_HOST).create(RedditProviderBase.class);
    }

    public static RedditClient getInstance(boolean isAuth){
        if(clientInstance == null)
            return new RedditClient(isAuth);
        else
            return clientInstance;
    }

    public Observable<PostItem> getSubredditListing(String query) {
        return provider.getSubreddit(query)
                .flatMap(response -> Observable.from(response.getData().getChildren()))
                .cast(RedditLink.class)
                .map(link -> new PostItem(link.getAuthor(), link.getCreatedUtc().getHourOfDay(),
                        link.getUrl(), link.getThumbnail(), link.getTitle(),
                        link.getDomain(), link.getSelftext(), link.getNumComments(), link.isSelf()))
                .concatMap(postItem -> {

                    //todo: request API
                    Observable<PostItem> imgurObservable = Observable.just(postItem)
                            .filter(item -> item.getType() == PostItem.Type.IMGUR_IMAGE);
                    //todo: convert to MP4 and set new link
                    Observable<PostItem> mp4Observable = Observable.just(postItem)
                            .filter(item -> item.getType() == PostItem.Type.GIF);
                    //leave out to render
                    Observable<PostItem> imageObservable = Observable.just(postItem)
                            .filter(item -> item.getType() == PostItem.Type.IMAGE);
                    //todo: render new view for text only
                    Observable<PostItem> selfObservable = Observable.just(postItem)
                            .filter(item -> item.getType() == PostItem.Type.SELF);
                    //todo: display new fragment with gridview
                    Observable<PostItem> galleryObservable = Observable.just(postItem)
                            .filter(item -> item.getType() == PostItem.Type.IMGUR_GALLERY
                                    || item.getType() == PostItem.Type.IMGUR_ALBUM
                                    || item.getType() == PostItem.Type.IMGUR_CUSTOM_GALLERY
                                    || item.getType() == PostItem.Type.IMGUR_TAG
                                    || item.getType() == PostItem.Type.IMGUR_SUBREDDIT);
                    //todo: display thumbnail with link to view full source along with ant self text
                    Observable<PostItem> defaultObservable = Observable.just(postItem)
                            .filter(item -> item.getType() == PostItem.Type.DEFAULT);

                    return Observable.merge(imageObservable, imgurObservable, mp4Observable, galleryObservable,
                            selfObservable, defaultObservable);
                }).doOnSubscribe(()-> RxBus.getDefault().send(new SubredditEvent()));
    }

}