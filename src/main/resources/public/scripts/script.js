console.log("Hello there :]");

$(".refresh").on("click",()=>{
    console.log("Overriden by jq");
    $.ajax({
        url: "/messages",
        method: "GET",
        dataType: "json",
        success: function(response) {
            console.log(response)
        },
        error: function(status, error) {
            // Handle any errors
            console.error(status, error);
        }
    });
});

