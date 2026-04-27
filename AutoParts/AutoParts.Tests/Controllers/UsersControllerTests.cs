using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using AutoParts.Controllers;
using AutoParts.Tests.Helpers;
using Xunit;

namespace AutoParts.Tests.Controllers;

public class UsersControllerTests
{
    private UsersController CreateController(string? userId, bool isAdmin)
    {
        var claims = new List<Claim>();

        if (userId != null)
            claims.Add(new Claim(ClaimTypes.NameIdentifier, userId));

        if (isAdmin)
            claims.Add(new Claim("app_metadata", "{\"role\":\"admin\"}"));

        var identity = new ClaimsIdentity(claims, "TestAuth");
        var controller = new UsersController();
        controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext { User = new ClaimsPrincipal(identity) }
        };
        return controller;
    }

    [Fact]
    public void GetCurrentUser_ReturnsUserId_AndIsAdminTrue_ForAdminUser()
    {
        var controller = CreateController("admin-user-456", isAdmin: true);

        var result = controller.GetCurrentUser();

        var ok = Assert.IsType<OkObjectResult>(result);
        var json = System.Text.Json.JsonSerializer.Serialize(ok.Value);
        Assert.Contains("admin-user-456", json);
        Assert.Contains("true", json);
    }

    [Fact]
    public void GetCurrentUser_ReturnsUserId_AndIsAdminFalse_ForRegularUser()
    {
        var controller = CreateController("regular-user-789", isAdmin: false);

        var result = controller.GetCurrentUser();

        var ok = Assert.IsType<OkObjectResult>(result);
        var json = System.Text.Json.JsonSerializer.Serialize(ok.Value);
        Assert.Contains("regular-user-789", json);
        Assert.Contains("false", json);
    }

    [Fact]
    public void GetCurrentUser_ReturnsUnauthorized_WhenNoUserIdClaim()
    {
        var controller = CreateController(userId: null, isAdmin: false);

        var result = controller.GetCurrentUser();

        Assert.IsType<UnauthorizedResult>(result);
    }
}
