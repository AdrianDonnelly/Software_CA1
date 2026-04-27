using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AutoParts.Controllers;

[Route("api/[controller]")]
[ApiController]
public class UsersController : ControllerBase
{
    [HttpGet("me")]
    [Authorize]
    public IActionResult GetCurrentUser()
    {
        var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value
                     ?? User.FindFirst("sub")?.Value;

        if (userId == null)
            return Unauthorized();

        var isAdmin = false;
        var appMetadata = User.FindFirst("app_metadata")?.Value;
        if (appMetadata != null)
        {
            try
            {
                var metadata = JsonSerializer.Deserialize<JsonElement>(appMetadata);
                isAdmin = metadata.TryGetProperty("role", out var role) && role.GetString() == "admin";
            }
            catch { }
        }

        return Ok(new { userId, isAdmin });
    }
}
