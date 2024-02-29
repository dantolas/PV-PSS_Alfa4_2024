console.log("Hello there :]");

document.querySelector(".refresh").addEventListener("click",()=>{
    console.log("xd");
})

$(".refresh").on("click",()=>{
    console.log("Overriden by jq");
})
