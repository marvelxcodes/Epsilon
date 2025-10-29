
export async function POST(request: Request) {
    console.log("before body")
    console.log(await request.json())
    return Response.json({
        message: "Updated"
    })
}
