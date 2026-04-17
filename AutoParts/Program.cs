using Microsoft.EntityFrameworkCore;
using AutoParts.Data;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddDbContext<AutoPartsDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("SupabaseConnection")));

builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
    });

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        policy => policy
            .AllowAnyOrigin()
            .AllowAnyMethod()
            .AllowAnyHeader());
});

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();

if (!app.Environment.IsDevelopment())
{
    app.UseHttpsRedirection();
}

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.MapGet("/api/health/database", async (AutoPartsDbContext db) =>
{
    try
    {
        var canConnect = await db.Database.CanConnectAsync();
        return Results.Ok(new 
        { 
            status = "healthy", 
            connected = canConnect,
            database = "Supabase",
            message = "Database connection successful" 
        });
    }
    catch (Exception ex)
    {
        return Results.Problem(
            detail: ex.Message, 
            statusCode: 500,
            title: "Database connection failed"
        );
    }
});

app.Run();