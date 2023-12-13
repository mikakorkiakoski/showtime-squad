package com.showtimesquad.showtimesquad.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.showtimesquad.showtimesquad.model.Group;
import com.showtimesquad.showtimesquad.model.User;
import com.showtimesquad.showtimesquad.model.UserList;
import com.showtimesquad.showtimesquad.model.response.MessageResponse;
import com.showtimesquad.showtimesquad.repository.GroupRepository;
import com.showtimesquad.showtimesquad.repository.ListRepository;
import com.showtimesquad.showtimesquad.repository.UserRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = { "/api/lists", "/list" })
public class ListController {

    @Autowired
    ListRepository listRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(listRepository.findAll());
    }

    @GetMapping("/{listname}")
    public ResponseEntity<?> getList(@PathVariable String listname, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<UserList> listOptional = listRepository.findByListName(listname);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("listname not found"));
        }

        UserList list = listOptional.get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        Group group = list.getGroup();

        if (!(list.getUser().equals(user) || group.getUsers().contains(user))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("User does not have access to this list"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/user/{username}") // get all lists of a user
    public ResponseEntity<?> getUserLists(@PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        }

        User user = userOptional.get();
        Optional<UserList> listOptional = listRepository.findByUser(user);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User has no lists"));
        }
        UserList lists = listOptional.get();
        return ResponseEntity.status(HttpStatus.OK).body(lists.getMovieIds());
    }

    @GetMapping("/group/{groupname}") // get all lists of a group
    public ResponseEntity<?> getGroupLists(@PathVariable String groupname,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<Group> groupOptional = groupRepository.findByGroupname(groupname);
        if (!groupOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Group not found"));
        }

        Group group = groupOptional.get();
        Optional<UserList> listOptional = listRepository.findByGroup(group);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Group has no lists"));
        }
        UserList lists = listOptional.get();
        return ResponseEntity.status(HttpStatus.OK).body(lists.getMovieIds());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUserList(
            @RequestParam String listname,
            String username,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        }

        User user = userOptional.get();
        UserList list = new UserList(listname, user, null);
        listRepository.save(list);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping("/create/group")
    public ResponseEntity<?> createGroupList(
            @RequestParam String listname,
            @RequestParam String groupname,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<Group> groupOptional = groupRepository.findByGroupname(groupname);
        if (!groupOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Group not found"));
        }

        Group group = groupOptional.get();
        UserList list = new UserList(listname, null, group);
        listRepository.save(list);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping("/create/user/group")
    public ResponseEntity<?> createUserList(
            @RequestParam String listname,
            @RequestParam String username,
            @RequestParam String groupname,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }
        if (!(groupRepository.existsByGroupname(groupname)
                || userRepository.existsByUsername(username))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User or Group not found"));
        }

        User user = userRepository.findByUsername(username).get();
        Group group = groupRepository.findByGroupname(groupname).get();
        UserList list = new UserList(listname, user, group);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PutMapping("/{listname}/rename")
    public ResponseEntity<?> renameList(
            @PathVariable String listname,
            @RequestParam String newname,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<UserList> listOptional = listRepository.findByListName(listname);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("List not found"));
        }

        UserList list = listOptional.get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        Group group = list.getGroup();

        if (!(list.getUser().equals(user) || group.getUsers().contains(user))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("User does not have access to this list"));
        }

        list.renameList(newname);
        listRepository.save(list);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PutMapping("/{listname}/add")
    public ResponseEntity<?> addMovie(
            @PathVariable String listname,
            @RequestParam Integer movieId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<UserList> listOptional = listRepository.findByListName(listname);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("List not found"));
        }

        UserList list = listOptional.get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        Group group = list.getGroup();

        if (!(list.getUser().equals(user) || group.getUsers().contains(user))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("User does not have access to this list"));
        }

        list.addMovie(movieId);
        listRepository.save(list);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PutMapping("/{listname}/remove")
    public ResponseEntity<?> removeMovie(
            @PathVariable String listname,
            @RequestParam Integer movieId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<UserList> listOptional = listRepository.findByListName(listname);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("List not found"));
        }

        UserList list = listOptional.get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        Group group = list.getGroup();

        if (!(list.getUser().equals(user) || group.getUsers().contains(user))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("User does not have access to this list"));
        }

        list.removeMovie(movieId);
        listRepository.save(list);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @DeleteMapping("/{listname}/delete")
    public ResponseEntity<?> deleteList(
            @PathVariable String listname,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not logged in"));
        }

        Optional<UserList> listOptional = listRepository.findByListName(listname);
        if (!listOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("List not found"));
        }

        UserList list = listOptional.get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        Group group = list.getGroup();

        if (!(list.getUser().equals(user) || group.getOwner().equals(user))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("User does not have access to this list"));
        }

        listRepository.delete(list);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("List deleted successfully"));
    }
}
