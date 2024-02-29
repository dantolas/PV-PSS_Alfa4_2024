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
            newMessages.addClass("messages");

            response.forEach(message => {
                var newMessage = $("<div></div>");
                newMessage.addClass("message");
                var author = $("<span></spam>");
                var content = $("<span></spam>");
                var id= $("<span></spam>");
                author.addClass("author");
                author.text(message.author);
                content.addClass("messageContent");
                content.text(message.msg);
                id.addClass("messageId");;
                id.text(message.id);

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

