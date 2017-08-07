PopularMovies app
=================

My Android app for the Udacity's Android Nanodegree Program. To run the app, you need to set the TheMovieDB API key as follows:

1. Get your API key from themoviedb.org.
2. Create a new file called `app/src/main/res/values/secrets.xml` and paste the following contents:

        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="themoviedb_api_key"></string>
        </resources>

3. Put your API key within the `<string name="themoviedb_api_key">` tag.
