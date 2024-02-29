console.log("Hello there :]");

$(".refresh").on("click",()=>{
    console.log("Overriden by jq");
    $.ajax({
        url: "/messages",
        method: "GET",
        dataType: "json",
        success: function(response) {
            $(".message").remove();
            response.forEach(message => {
                console.out(message);
            });
        },
        error: function(status, error) {
            alert("Couldn't refresh page");
        }
    });
});

