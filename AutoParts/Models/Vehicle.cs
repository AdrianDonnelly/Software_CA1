using System.ComponentModel.DataAnnotations;

namespace AutoParts.Models;

public class Vehicle
{
    [Key]
    public int VehicleId { get; set; }
    
    [Required]
    [MaxLength(50)]
    public string Make { get; set; } = string.Empty;
    
    [Required]
    [MaxLength(50)]
    public string Model { get; set; } = string.Empty;
    
    [Range(1990, 2030)]
    public int Year { get; set; }
    
    [MaxLength(100)]
    public string EngineType { get; set; } = string.Empty;
    
    public string? ImageUrl { get; set; }
    
    [MaxLength(20)]
    public string Category { get; set; } = string.Empty;
    
    public ICollection<AutoPart> AutoParts { get; set; } = new List<AutoPart>();
}