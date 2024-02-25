package com.kuta.web.backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RouteController {

    private static final List<MessageModel> messages = new ArrayList<>(){{
        add(new MessageModel("kuta", "Hello there :]",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("not-peer", "I'm not a peer",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("idk", "I dunno man",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("kuta", "Hello there :]",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("not-peer", "I'm not a peer",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("idk", "I dunno man",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("kuta", "Hello there :]",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("not-peer", "I'm not a peer",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("idk", "I dunno man",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("kuta", "Hello there :]",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("not-peer", "I'm not a peer",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("idk", "I dunno man",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("kuta", "Hello there :]",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("not-peer", "I'm not a peer",Long.toString(System.currentTimeMillis())));
        add(new MessageModel("idk", "I dunno man",Long.toString(System.currentTimeMillis())));
    }};

    private final List<MessageModel> msgs;
    public RouteController(){
        this.msgs = messages;
    }

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("msgs", msgs);
		return "greeting";
	}
	@GetMapping("/")
	public String index(Model model) { 
        model.addAttribute("msgs",msgs);
		return "index";
	}

}