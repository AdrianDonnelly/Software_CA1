using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AutoParts.Models;

public class AutoPart
{
    [Key]
    public int PartId { get; set; }
    
    [Required]
    [MaxLength(50)]
    public string PartNumber { get; set; } = string.Empty;
    
    [Required]
    [MaxLength(200)]
    public string Name { get; set; } = string.Empty;
    
    [Required]
    [MaxLength(50)]
    public string Category { get; set; } = string.Empty;
    
    [MaxLength(100)]
    public string Manufacturer { get; set; } = string.Empty;
    
    [Column(TypeName = "decimal(18,2)")]
    [Range(0, 999999)]
    public decimal Price { get; set; }
    
    [Range(0, 10000)]
    public int StockQuantity { get; set; }
    
    //FK
    [Required]
    public int VehicleId { get; set; }
    
    // Nav
    [ForeignKey("VehicleId")]
    public Vehicle? Vehicle { get; set; }
    
    [MaxLength(1000)]
    public string? Description { get; set; }
    
    public string? ImageUrl { get; set; }
    
    [MaxLength(20)]
    public string Condition { get; set; } = string.Empty;
}