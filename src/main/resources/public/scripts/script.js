console.log("Hello there :]");

$(".refresh").on("click",()=>{
    refresh();
});

$(".send").on("click",(event)=>{
    event.preventDefault();
    send();
});

function send(){
    var messageVal = $(".msg").val();
    var peerVal = $(".select").val();
    $.ajax({
        url: "/send",
        method: "GET",
        dataType: "json",
        data:{
            message:messageVal,
            peer:peerVal
            },
        success: function(response) {
            alert("Message sent");
            refresh();
        },
        error: function(status, error) {
            alert("Error occured, message not sent");
        }
    });

}

function refresh(){
    $.ajax({
        url: "/messages",
        method: "GET",
        dataType: "json",
        success: function(response) {
            $(".message").remove();

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

                $(newMessage).prepend(id);
                $(newMessage).prepend(content);
                $(newMessage).prepend(author);

                $(".messages").prepend(newMessage);
            });
            //$(".messages").replaceWith(newMessages);
        },
        error: function(status, error) {
            alert("Couldn't refresh page");
        }
    });
}

