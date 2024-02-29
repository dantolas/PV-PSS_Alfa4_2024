console.log("Hello there :]");

$(".refresh").on("click",()=>{
    console.log("Overriden by jq");
    $.ajax({
        url: "/messages",
        method: "GET",
        dataType: "json",
        success: function(response) {
            $(".message").remove();
            var newMessages = $("<div></div>");
            newMessages.class = "messages";

            response.forEach(message => {
                var newMessage = $("<div></div>");
                var author = $("<span></spam>");
                var content = $("<span></spam>");
                var id= $("<span></spam>");
                author.class = "author";
                author.text = message.author;
                content.class = "messageContent";
                content.text=message.msg;
                id.class = "messageId";
                id.text = message.id;

                $(newMessage).prepend(author);
                $(newMessage).prepend(content);
                $(newMessage).prepend(id);

                $(newMessages).prepend(newMessage);
            });
            $(".messages").replaceWith(newMessages);
        },
        error: function(status, error) {
            alert("Couldn't refresh page");
        }
    });
});

