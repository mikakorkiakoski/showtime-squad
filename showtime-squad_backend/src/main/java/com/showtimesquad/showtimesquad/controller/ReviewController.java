package com.showtimesquad.showtimesquad.controller;

import com.showtimesquad.showtimesquad.model.MovieReviews;
import com.showtimesquad.showtimesquad.model.User;
import com.showtimesquad.showtimesquad.model.request.MovieReviewsRequest;
import com.showtimesquad.showtimesquad.model.response.MovieReviewsResponse;
import com.showtimesquad.showtimesquad.repository.ReviewRepository;
import com.showtimesquad.showtimesquad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/")
    public ResponseEntity<MovieReviewsResponse> createReview(@RequestBody MovieReviewsRequest movieReviewsRequest) {
        try {
            // Validate the request if necessary
            if (movieReviewsRequest.getReviewStars() == null || movieReviewsRequest.getUserId() == null || movieReviewsRequest.getMovieApi() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Fetch the user from the repository
            User user = userRepository.findById(movieReviewsRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Save the review to the database
            MovieReviews movieReviews = new MovieReviews();
            movieReviews.setUser(user);
            movieReviews.setMovieApi(movieReviewsRequest.getMovieApi());
            movieReviews.setReviewStars(movieReviewsRequest.getReviewStars());
            movieReviews.setReviewText(movieReviewsRequest.getReviewText());

            reviewRepository.save(movieReviews);

            // Convert and return the response
            MovieReviewsResponse finalMovieReviewsResponse = new MovieReviewsResponse(
                    movieReviews.getId(),
                    movieReviews.getUser().getId(),
                    movieReviews.getMovieApi(),
                    movieReviews.getReviewStars(),
                    movieReviews.getReviewText()
            );

            return new ResponseEntity<>(finalMovieReviewsResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   @GetMapping("/{movies_api_id}")
public ResponseEntity<List<MovieReviewsResponse>> getReviewsByMovieApi(@PathVariable("movies_api_id") Integer movieApi) {
    try {

        // Fetch the reviews from the repository
        List<MovieReviews> movieReviewsList = reviewRepository.findByMovieApi(movieApi);

        // Convert and return the response
        List<MovieReviewsResponse> responseList = movieReviewsList.stream()
                .map(movieReviews -> new MovieReviewsResponse(
                        movieReviews.getId(),
                        movieReviews.getUser().getId(),
                        movieReviews.getMovieApi(),
                        movieReviews.getReviewStars(),
                        movieReviews.getReviewText()
                ))
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    } catch (Exception e) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
}
